import java.net.*;
import java.util.Scanner;
import java.io.*;
import java.util.*;

public class BookClient {
  public static int clientId;
  public static boolean setTCP;
  public static InetAddress addr;
  public static String hostAddress = "localhost";
  public static int tcpPort = 7000;// hardcoded -- must match the server's tcp port
  public static int udpPort = 8000;// hardcoded -- must match the server's udp port
  public static DatagramSocket datagramSocket;
  public static DatagramPacket sentPacket;
  public static DatagramPacket receivePacket;
  private static Socket sock;
  private static OutputStream output;
  private static InputStream input;
  private static InputStreamReader inputStream;
  private static BufferedReader bufferInput;
  private static String message;
  private static PrintStream print;
  private static PrintWriter fileWriter;
  private static FileWriter file;

  public static void main (String[] args) {

    if (args.length != 2) {
      System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
      System.out.println("\t(1) <command-file>: file with commands to the server");
      System.out.println("\t(2) client id: an integer between 1..9");
      System.exit(-1);
    }

    String commandFile = args[0];
    clientId = Integer.parseInt(args[1]);
    String fileString = "out_"+clientId+".txt";
    try {
      file = new FileWriter(fileString);
      fileWriter = new PrintWriter(file);
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      addr = InetAddress.getByName(hostAddress);
      datagramSocket = new DatagramSocket();
    } catch(UnknownHostException e) {
      System.err.println(e);
    } catch (SocketException e) {
      e.printStackTrace();
    }

    try {
      sock = new Socket(hostAddress, tcpPort);
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch(IOException e) {
      e.printStackTrace();
    }

    try {
      input = sock.getInputStream();
    } catch(IOException e) {
      e.printStackTrace();
    }

    try {
      output = sock.getOutputStream();
    } catch(IOException e) {
      e.printStackTrace();
    }

    print = new PrintStream(output);
    inputStream = new InputStreamReader(input);
    bufferInput = new BufferedReader(inputStream);
    try {
        Scanner sc = new Scanner(new FileReader(commandFile));

        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");
          if (tokens[0].equals("setmode")) {
            if(tokens[1].equals("T")) {
              setTCP = true;
            }
            else {
              setTCP = false;
            }
          } else {
            if (setTCP) {
              sendTCP(cmd);
            } else {
              sendUDP(cmd);
            }
          }

        }
    } catch (FileNotFoundException e) {
	e.printStackTrace();
    }
    fileWriter.close();
  }



  public static void sendTCP(String input) {
    print.println(input);
    char[] buffer = new char[1500];
    int bytesRead = 0;
    try {
      bytesRead = bufferInput.read(buffer, 0, 1500);
      message = new String(buffer);
      if(message.substring(0, bytesRead).equals("exit")) {
        sock.close();
        return;
      }
      System.out.println(message.substring(0, bytesRead));
      fileWriter.println(message.substring(0, bytesRead));
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  public static void sendUDP(String input) {
    byte[] buffer = new byte[input.length()];
    buffer = input.getBytes();
    sentPacket = new DatagramPacket(buffer, buffer.length, addr, udpPort);
    try {
      datagramSocket.send(sentPacket);
    } catch(IOException e) {
      e.printStackTrace();
    }

    byte[] returnBuff = new byte[65507];
    receivePacket = new DatagramPacket(returnBuff, returnBuff.length);
    try {
      datagramSocket.receive(receivePacket);
    } catch (IOException e) {
      e.printStackTrace();
    }

    message = new String(receivePacket.getData(), 0, receivePacket.getLength());
    if(message.equals("exit")) {
      datagramSocket.close();
      return;
    }
    fileWriter.println(message);
    System.out.println(message);

  }
}
