/*
Mohamed Nasreldin man2766
Hamza Ghani hhg263
*/

import java.net.*;
import java.util.Scanner;
import java.io.*;
import java.util.*;

public class BookClient {
  private String hostAddress;
  private int tcpPort;
  private int udpPort;
  private InetAddress hostIP;
  private String connectionMode;
  private DatagramSocket UDPSocket;
  private DatagramPacket receiveUDP;
  private byte[] buf, rbuf;
  private Socket TCPSocket;
  private static Integer clientId;
  private Scanner in;
  private PrintStream out;
  List<String> outFileData = new ArrayList<>();


  public BookClient(String hostAddress, int tcpPort, int udpPort) throws IOException {
    this.hostAddress = hostAddress;
    this.tcpPort = tcpPort;
    this.udpPort = udpPort;
    this.hostIP = InetAddress.getByName(hostAddress);
    this.connectionMode = "U";
    this.UDPSocket = new DatagramSocket();
    this.TCPSocket = new Socket(this.hostAddress, this.tcpPort);
    this.in = new Scanner(TCPSocket.getInputStream());
    this.out = new PrintStream(TCPSocket.getOutputStream());
    this.rbuf = new byte[1024];

  }

  public static void main (String[] args) throws IOException {
    String hostAddress;
    int tcpPort;
    int udpPort;

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

    try {
        BookClient client = new BookClient(hostAddress, tcpPort, udpPort);
        Scanner sc = new Scanner(new FileReader(commandFile));

        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");

          if (tokens[0].equals("setmode")) {
            client.setmode(tokens[1]);
          }
          else if (tokens[0].equals("borrow")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
            client.sendToServer(cmd);
          } else if (tokens[0].equals("return")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
            client.sendToServer(cmd);
          } else if (tokens[0].equals("inventory")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
            client.sendToServer(cmd);
          } else if (tokens[0].equals("list")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
            client.sendToServer(cmd);
          } else if (tokens[0].equals("exit")) {
            client.exit();
          } else {
            System.out.println("ERROR: No such command");
          }
        }
    } catch (FileNotFoundException e) {
	e.printStackTrace();
    }
  }

  private void sendToServer(String token) throws IOException {
    buf = token.getBytes();
    String returned = "", retTemp;
    if(connectionMode.equals("U")){
      UDPSocket.send(new DatagramPacket(buf, buf.length, hostIP, udpPort));
      receiveUDP = new DatagramPacket(rbuf, rbuf.length);
      UDPSocket.receive(receiveUDP);
      returned = new String(receiveUDP.getData(), 0, receiveUDP.getLength());

    }
    else{
      out.println(token);
      out.flush();
      while(in.hasNextLine()){
        retTemp = in.nextLine();
        if(retTemp.equals("end"))
          break;
        returned += retTemp;

      }
    }
    returned = returned.replace("&", "\n");
    outFileData.add(returned);

  }

  public synchronized void setmode(String mode){
      connectionMode = mode;
  }

  public synchronized void exit(){
    PrintWriter writer = null;
    try {
      writer = new PrintWriter("out_" + clientId + ".txt", "UTF-8");
    } catch (FileNotFoundException e) {
    } catch (UnsupportedEncodingException e) {}
    for(int i=0; i<outFileData.size(); i++){
      writer.println(outFileData.get(i));
    }
    writer.close();
    try {
      writer = new PrintWriter("inventory.txt", "UTF-8");
    } catch (FileNotFoundException e) {
    } catch (UnsupportedEncodingException e) {
    }
    try {
      outFileData.clear();
      connectionMode = "T";
      sendToServer("inventory");
      for(int i=0; i<outFileData.size(); i++){
        writer.println(outFileData.get(i));
      }
      writer.close();
    } catch (IOException e) {
    }
    UDPSocket.close();
    try {
      TCPSocket.close();
    } catch (IOException e) {}
  }



}
