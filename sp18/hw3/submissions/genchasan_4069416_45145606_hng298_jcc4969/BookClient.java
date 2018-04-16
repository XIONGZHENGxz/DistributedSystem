//package homework3;

import java.util.Scanner;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
public class BookClient {
  public static void main (String[] args) {
    String hostAddress;
    int tcpPort;
    int udpPort;
    int len = 1024;
    int clientId;
    PrintWriter printWriter = null;
    DatagramSocket UDPsocket;
    Socket TCPsocket;
    byte[] rbuffer = new byte[len];
    DatagramPacket sPacket, rPacket;
    String mode = "U";
    final String endMsg = "224cbea3-ffc8-48e9-8aad-1079474b35db";
    
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
    String outFile = "out_" + clientId + ".txt";
    
    try {
		FileWriter fileWriter = new FileWriter(outFile);
		printWriter = new PrintWriter(fileWriter);
	} catch (IOException e1) {
		e1.printStackTrace();
	}
    

    try {
        Scanner sc = new Scanner(new FileReader(commandFile));
		UDPsocket = new DatagramSocket();
		InetAddress ia = InetAddress.getByName(hostAddress) ;
		TCPsocket = new Socket(hostAddress, tcpPort);
		//Scanner in = new Scanner
		BufferedReader in = new BufferedReader(new InputStreamReader(TCPsocket.getInputStream()));
        PrintWriter out = new PrintWriter(TCPsocket.getOutputStream(), true);
        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");

          if (tokens[0].equals("setmode")) {
        	  if(tokens[1].equals("T")) {
        		  mode = "T";
        	  }else {
        		  mode = "U";
        	  }
          }
          else if (tokens[0].equals("borrow")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
        	  if(mode.equals("T")) {
        		  out.println(cmd);
                  for (String response = in.readLine(); response != null && !response.equals(endMsg); response = in.readLine()) {
                      if (response.equals(""))
                          continue;
                        printWriter.println(response);
                  }
        	  }else {
        		  byte[] buffer = new byte[cmd.length()];
        		  buffer = cmd.getBytes();
        		  sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);  //add host address and port
        		  UDPsocket.send(sPacket);
        		  rPacket = new DatagramPacket(rbuffer, rbuffer.length);
        		  UDPsocket.receive(rPacket);
        		  String retstring = new String(rPacket.getData(), 0, rPacket.getLength());
        		  //write to file
        		  printWriter.print(retstring);
        	  }
          } else if (tokens[0].equals("return")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
        	  if(mode.equals("T")) {
        		  out.println(cmd);
                  for (String response = in.readLine(); response != null && !response.equals(endMsg); response = in.readLine()) {
                      if (response.equals(""))
                          continue;
                        printWriter.println(response);
                  }
        	  }else {
        		  byte[] buffer = new byte[cmd.length()];
        		  buffer = cmd.getBytes();
        		  sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);  //add host address and port
        		  UDPsocket.send(sPacket);
        		  rPacket = new DatagramPacket(rbuffer, rbuffer.length);
        		  UDPsocket.receive(rPacket);
        		  String retstring = new String(rPacket.getData(), 0, rPacket.getLength());
        		  printWriter.print(retstring);
        	  }
          } else if (tokens[0].equals("inventory")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
        	  if(mode.equals("T")) {
        		  out.println(cmd);
                  for (String response = in.readLine(); response != null && !response.equals(endMsg); response = in.readLine()) {
                      if (response.equals(""))
                          continue;
                        printWriter.println(response);
                  }
        	  }else {
        		  byte[] buffer = new byte[cmd.length()];
        		  buffer = cmd.getBytes();
        		  sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);  //add host address and port
        		  UDPsocket.send(sPacket);
        		  rPacket = new DatagramPacket(rbuffer, rbuffer.length);
        		  UDPsocket.receive(rPacket);
        		  String retstring = new String(rPacket.getData(), 0, rPacket.getLength());
        		  printWriter.print(retstring);
        	  }
          } else if (tokens[0].equals("list")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
        	  if(mode.equals("T")) {
        		  out.println(cmd);
                  for (String response = in.readLine(); response != null && !response.equals(endMsg); response = in.readLine()) {
                      if (response.equals(""))
                          continue;
                        printWriter.println(response);
                  }
        	  }else {
        		  byte[] buffer = new byte[cmd.length()];
        		  buffer = cmd.getBytes();
        		  sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);  //add host address and port
        		  UDPsocket.send(sPacket);
        		  rPacket = new DatagramPacket(rbuffer, rbuffer.length);
        		  UDPsocket.receive(rPacket);
        		  String retstring = new String(rPacket.getData(), 0, rPacket.getLength());
        		  printWriter.print(retstring);
        	  }
          } else if (tokens[0].equals("exit")) {
        	  printWriter.close();
          } else {
            System.out.println("ERROR: No such command");
          }
        }
    } catch (IOException e) {
	e.printStackTrace();
    }
  }
}
