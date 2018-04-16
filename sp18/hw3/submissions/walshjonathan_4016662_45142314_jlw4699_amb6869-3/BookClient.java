import java.util.Scanner;
import java.io.*;
import java.util.*;
import java.net.*;

public class BookClient {

    static int port = 8000;
    static Socket tcpsocket;
    static BufferedReader tcpin;
    static PrintWriter tcpout;
    static boolean invalidTCP;

  public static void main (String[] args) {
    String commandFile;
    int clientId;
    boolean usingUDP;
    invalidTCP = true;


    if (args.length != 2) {
      System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
      System.out.println("\t(1) <command-file>: file with commands to the server");
      System.out.println("\t(2) client id: an integer between 1..9");
      System.exit(-1);
    }

    commandFile = args[0];
    clientId = Integer.parseInt(args[1]);
    usingUDP = true;

    try {
        String p = communicate("request U", usingUDP);
        System.out.println("first request for UDP");
        try {
            Thread.sleep(10);
        } catch (Exception e){
            e.printStackTrace();
        }
        port = Integer.parseInt(p.split(" ")[1]);
        Scanner sc = new Scanner(new FileReader(commandFile));

        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");

          if (tokens[0].equals("setmode")) {
            // set the mode of communication for sending commands to the server 
            usingUDP = isUsingUDP(tokens[1], usingUDP);
              System.out.println("New Mode: "+usingUDP);
              try {
                  invalidTCP = true;
                  tcpsocket.close();
              } catch (Exception e){
                  e.printStackTrace();
              }
              try {
                Thread.sleep(10);
              } catch (Exception e) {
                  e.printStackTrace();
              }
                
          }
          else if (tokens[0].equals("borrow")) {
            // send appropriate command to the server and display the appropriate responses form the server
            String restring = communicate(cmd, usingUDP);
            logRestring(restring, clientId);
          } else if (tokens[0].equals("return")) {
            // send appropriate command to the server and display the appropriate responses form the server
            String restring = communicate(cmd, usingUDP);
            logRestring(restring, clientId);
          } else if (tokens[0].equals("inventory")) {
            // send appropriate command to the server and display the appropriate responses form the server
            String restring = communicate(cmd, usingUDP);
            logRestring(restring, clientId);
          } else if (tokens[0].equals("list")) {
            // send appropriate command to the server and display the appropriate responses form the server
            String restring = communicate(cmd, usingUDP);
            logRestring(restring, clientId);
          } else if (tokens[0].equals("exit")) {
            // send appropriate command to the server 
            String restring = communicate(cmd, usingUDP);
            logRestring(restring, clientId);  //maybe need to not wait?
          } else {
            System.out.println("ERROR: No such command");
          }
        }
    } catch (FileNotFoundException e) {
	e.printStackTrace();
    }
  }

  private static boolean isUsingUDP(String mode, boolean usingUDP) {
      if (mode.equals("T")) {
          System.out.println("requesting TCP");
          String[] s = communicate("request T", usingUDP).split(" ");
          System.out.println("something "+s[0]);
          System.out.println(s[1]);

        port = Integer.parseInt(s[1]);
        return false;
      }
      else {
          System.out.println("requesting UDP");
        port = Integer.parseInt(communicate("request U", usingUDP).split(" ")[1]);
        return true;
      }
  }

  private static String communicate(String msg, boolean usingUDP) {
      System.out.println("UDP: "+usingUDP);
      if (usingUDP)
        return communicateUDP(msg);
      else
        return communicateTCP(msg);
  }

  private static String communicateUDP(String msg) {
      String hostAddress = "localhost";
      int udpPort = 8000;// hardcoded -- must match the server's udp port
      try {
          InetAddress ia = InetAddress.getByName(hostAddress);
          DatagramSocket datasocket = new DatagramSocket();
          byte[] buffer = new byte[msg.length()];
          buffer = msg.getBytes();
          System.out.println("connecting to port "+port);
          DatagramPacket sPacket = new DatagramPacket(buffer, buffer.length, ia, port);
          datasocket.send(sPacket);
          byte[] rBuffer = new byte[1024];
          DatagramPacket rPacket = new DatagramPacket(rBuffer, rBuffer.length);
          datasocket.receive(rPacket);
          String restring = new String(rPacket.getData(), 0, rPacket.getLength());
          System.out.println("UDP recev: "+restring);

          String[] lines = restring.split("~newLine~");
          String result = new String();
          for (String s : lines){
              result+=s+"\n";
          }
          result = result.substring(0, result.length() - 1);

          System.out.println("UDP parse: "+result);

          datasocket.close();
          return result;
      } catch (UnknownHostException e) {
          e.printStackTrace();
          return "";
      } catch (SocketException e) {
          e.printStackTrace();
          return "";
      } catch (IOException e) {
          e.printStackTrace();
          return "";
      }

  }

  private static String communicateTCP(String msg) {
      if(invalidTCP){
        String hostAddress = "localhost";
        int tcpPort = 7000;// hardcoded -- must match the server's tcp port
        try{
            tcpsocket = new Socket(hostAddress, port);
            tcpin = new BufferedReader(new InputStreamReader(tcpsocket.getInputStream()));
            tcpout = new PrintWriter(tcpsocket.getOutputStream(), true);
            invalidTCP = false;
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
      }

      tcpout.println(msg);

      String restring = null;
      try {
          restring = tcpin.readLine();
      } catch (IOException e1) {
          e1.printStackTrace();
      }

      String[] lines = restring.split("~newLine~");
      String result = new String();
      for (String s : lines){
          result+=s+"\n";
      }
      result = result.substring(0, result.length() - 1);

      System.out.println("TCP recev: "+result);
      return result;
  }

  private static void logRestring(String restring, int clientId) {
      if (restring.equals(""))
        return;

      String filename = "out_" + Integer.toString(clientId) + ".txt";
      boolean appendMode = true;
      try(FileWriter fw = new FileWriter(filename, appendMode);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
      {
        out.println(restring);
      } catch (IOException e) {
          e.printStackTrace();
      }
  }

}