/*
 * rsr873
 * vks377
 */

import java.net.*;
import java.util.Scanner;
import java.io.*;
import java.util.*;
public class BookClient {
  private Socket server;
  private Scanner din;
  private PrintStream pout;
  private InetAddress ia;
  private DatagramSocket datasocket;
  private DatagramPacket sPacket, rPacket;
  byte[] rBuffer = new byte[65507];
  public static void main (String[] args) throws IOException {
    String hostAddress;
    int tcpPort;
    int udpPort;
    int clientId;
    boolean isTCP;
    boolean running = true;

    if (args.length != 2) {
      System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
      System.out.println("\t(1) <command-file>: file with commands to the server");
      System.out.println("\t(2) client id: an integer between 1..9");
      System.exit(-1);
    }

    String commandFile = args[0];
    clientId = Integer.parseInt(args[1]);
    hostAddress = "localhost";
    tcpPort = 7000;// hardcoded -- must match the server's tcp port
    udpPort = 8000;// hardcoded -- must match the server's udp port
    isTCP = false; // default mode is UDP

    // hookup output file
    String fileName = "out_" + clientId + ".txt";
    File file = new File(fileName);
    PrintWriter poutFile = new PrintWriter(new FileWriter(file));

    try {
        Scanner sc = new Scanner(new FileReader(commandFile));
        BookClient client = new BookClient();
        client.getTCPPort(hostAddress, tcpPort);
        client.getUDPPort(hostAddress);

        while(sc.hasNextLine() && running) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");

          if (tokens[0].equals("setmode")) {
            // TODO: set the mode of communication for sending commands to the server
            if(tokens[1].equals("T"))
              isTCP = true;
            else
              isTCP = false;
          }
          else if (tokens[0].equals("borrow")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
            String studentName = tokens[1];
            String bookName = "";
            for(int i = 2; i < tokens.length - 1; i++)
              bookName += tokens[i] + " ";
            bookName += tokens[tokens.length - 1];
            client.borrow(isTCP, studentName, bookName, hostAddress, tcpPort, udpPort, poutFile);
          } else if (tokens[0].equals("return")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
            String recordID = tokens[1];
            client.returnBook(isTCP, recordID, hostAddress, tcpPort, udpPort, poutFile);
          } else if (tokens[0].equals("inventory")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
            client.inventory(isTCP, hostAddress, tcpPort, udpPort, poutFile);
          } else if (tokens[0].equals("list")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
            String studentName = tokens[1];
            client.listStudent(isTCP, studentName, hostAddress, tcpPort, udpPort, poutFile);
          } else if (tokens[0].equals("exit")) {
            // TODO: send appropriate command to the server
            client.exit(isTCP, hostAddress, tcpPort, udpPort);
            poutFile.close();
            running = false;
          } else {
            System.out.println("ERROR: No such command");
          }
        }
    } catch (Exception e) {
	e.printStackTrace();
    }
  }

  public void getTCPPort(String hostAddress, int port) throws IOException {
    server = new Socket(hostAddress, port);
    din = new Scanner(server.getInputStream());
    pout = new PrintStream(server.getOutputStream());
  }

  public void getUDPPort(String hostAddress) throws UnknownHostException, SocketException {
    ia = InetAddress.getByName(hostAddress);
    datasocket = new DatagramSocket();
  }

  public void borrow(boolean isTCP, String studentName, String bookName, String hostAddress, int tcpPort, int udpPort, PrintWriter poutFile) throws IOException {
    if(isTCP) {
      //getTCPPort(hostAddress, tcpPort);
      pout.println("borrow " + studentName + " " + bookName);
      pout.flush();
      String message = din.nextLine();
      poutFile.println(message);
      poutFile.flush();
    }
    else {
      // UDP code
      String command = ("borrow " + studentName + " " + bookName);
      byte[] buffer = command.getBytes();
      sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
      datasocket.send(sPacket);
      rPacket = new DatagramPacket(rBuffer, rBuffer.length);
      datasocket.receive(rPacket);
      String message = new String(rPacket.getData(), 0, rPacket.getLength());
      poutFile.println(message);
      poutFile.flush();
    }
  }

  public void returnBook(boolean isTCP, String recordID, String hostAddress, int tcpPort, int udpPort, PrintWriter poutFile) throws IOException {
    if(isTCP) {
      //getTCPPort(hostAddress, tcpPort);
      pout.println("return " + recordID);
      pout.flush();
      String message = din.nextLine();
      poutFile.println(message);
      poutFile.flush();
    }
    else {
      // UDP code
      String command = "return " + recordID;
      byte[] buffer = command.getBytes();
      sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
      datasocket.send(sPacket);
      rPacket = new DatagramPacket(rBuffer, rBuffer.length);
      datasocket.receive(rPacket);
      String message = new String(rPacket.getData(), 0, rPacket.getLength());
      poutFile.println(message);
      poutFile.flush();
    }
  }

  public void inventory(boolean isTCP, String hostAddress, int tcpPort, int udpPort, PrintWriter poutFile) throws IOException {
    if(isTCP) {
      //getTCPPort(hostAddress, tcpPort);
      pout.println("inventory");
      pout.flush();
      String message = din.nextLine();
      int lines = Integer.valueOf(message);
      for(int i = 0; i < lines; i++) {
        message = din.nextLine();
        poutFile.println(message);
      }
      poutFile.flush();
    }
    else {
      // UDP code
      String command = "inventory";
      byte[] buffer = command.getBytes();
      sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
      datasocket.send(sPacket);
      rPacket = new DatagramPacket(rBuffer, rBuffer.length);
      datasocket.receive(rPacket);
      String message = new String(rPacket.getData(), 0, rPacket.getLength());
      Scanner st = new Scanner(message);
      int lines = Integer.parseInt(st.nextLine());
      for(int i = 0; i < lines; i++) {
        String line = st.nextLine();
        poutFile.println(line);
      }
      poutFile.flush();
    }
  }

  public void listStudent(boolean isTCP, String studentName, String hostAddress, int tcpPort, int udpPort, PrintWriter poutFile) throws IOException {
    if(isTCP) {
      //getTCPPort(hostAddress, tcpPort);
      pout.println("list " + studentName);
      pout.flush();
      String message = din.nextLine();
      int lines = Integer.valueOf(message);
      for(int i = 0; i < lines; i++) {
        message = din.nextLine();
        poutFile.println(message);
      }
      poutFile.flush();
    }
    else {
      // UDP code
      String command = "list " + studentName;
      byte[] buffer = command.getBytes();
      sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
      datasocket.send(sPacket);
      rPacket = new DatagramPacket(rBuffer, rBuffer.length);
      datasocket.receive(rPacket);
      String message = new String(rPacket.getData(), 0, rPacket.getLength());
      Scanner st = new Scanner(message);
      int lines = Integer.parseInt(st.nextLine());
      for(int i = 0; i < lines; i++) {
        String line = st.nextLine();
        poutFile.println(line);
      }
      poutFile.flush();
    }
  }

  public void exit(boolean isTCP, String hostAddress, int tcpPort, int udpPort) throws IOException {
    if(isTCP) {
      //getTCPPort(hostAddress, tcpPort);
      pout.println("exit");
      pout.flush();
    }
    else {
      // UDP code
      String command = "exit";
      byte[] buffer = command.getBytes();
      sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
      datasocket.send(sPacket);
    }
  }


}
