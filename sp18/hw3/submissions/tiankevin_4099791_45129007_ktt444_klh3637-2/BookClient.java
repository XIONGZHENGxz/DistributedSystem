/**
 * HW3 EE360P
 * Authors: Kevin Tian ktt444
 * 			Kenneth Hall klh3637
 */

import java.util.Scanner;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
public class BookClient {
  public static void main (String[] args) {
    String hostAddress;
    int tcpPort;
    int udpPort;
    int clientId;
    boolean udp;
    DatagramSocket udpSocket;
    Socket tcpSocket;
    BufferedReader inFromServer;
    DataOutputStream outToServer; 
    InetAddress IPAddress;
    
    if (args.length != 2) {
      System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
      System.out.println("\t(1) <command-file>: file with commands to the server");
      System.out.println("\t(2) client id: an integer between 1..9");
      System.exit(-1);
    }
    String commandFile = args[0];
    clientId = Integer.parseInt(args[1]);
    
    hostAddress = "localhost";
    tcpPort = 9000;// hardcoded -- must match the server's tcp port
    udpPort = 8080;// hardcoded -- must match the server's udp port
    udp = true;
    byte[] sendData = new byte[1024];
	byte[] receiveData = new byte[1024];

    
    try {
    	PrintWriter writer = new PrintWriter("out_" + clientId + ".txt", "UTF-8");
        Scanner sc = new Scanner(new FileReader(commandFile));
		
        //udp init
        udpSocket = new DatagramSocket();
        IPAddress = InetAddress.getByName(hostAddress);
        
        //initial contact
        String init = " initial ";
        sendData = init.getBytes();
		DatagramPacket sP = new DatagramPacket(sendData, sendData.length, IPAddress, udpPort);
		udpSocket.send(sP);
		
		//receive ports
		DatagramPacket tempPacket = new DatagramPacket(receiveData, receiveData.length);
		udpSocket.receive(tempPacket);
		String message = new String(tempPacket.getData());
		String[] ports = message.split(" ");
		System.out.println("new ports: " + ports[1] + " " + ports[2]);
		
		tcpPort = Integer.parseInt(ports[1]);
		udpPort = Integer.parseInt(ports[2]);
		
        //tcp init
        tcpSocket = new Socket(hostAddress, tcpPort);
        outToServer = new DataOutputStream(tcpSocket.getOutputStream());
        inFromServer = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));

        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");
          System.out.println(cmd);

          if (tokens[0].equals("setmode")) {
            // TODO: set the mode of communication for sending commands to the server 
        	  if(tokens[1].equals("T")) {
        		  if(udp) {
        			  sendData = new String("tcpchange").getBytes();
            		  DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, udpPort);
            		  udpSocket.send(sendPacket);
            		  udp = false;        		          			  
        		  }      		  
        	  }else if(tokens[1].equals("U")) {
        		  if(!udp) {
        			  outToServer.writeBytes("udpchange" + '\n');
            		  udp = true;   
        		  } 
        	  }    	  
          }
          else if (tokens[0].equals("borrow") || tokens[0].equals("return") || tokens[0].equals("list")
        		  || tokens[0].equals("inventory") || tokens[0].equals("exit")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
        	  
        	  String sentence = "";
        	  if(tokens[0].equals("borrow")) {
        		  sentence += tokens[0];
        		  for(int i = 1; i < tokens.length; i++) {
        			  sentence += " " + tokens[i];
        		  }
        	  }
        	  else if(tokens[0].equals("return") || tokens[0].equals("list")) sentence = tokens[0] + " " + tokens[1];
        	  else if(tokens[0].equals("inventory") || tokens[0].equals("exit"))	sentence = tokens[0];  
    		  System.out.println(sentence);
        	  
        	  if(udp) {
        		  
        		  /*----------------SEND-----------------*/
        		  sendData = (sentence).getBytes();
        		  DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, udpPort);
        		  udpSocket.send(sendPacket);
        		  
        		  if(!tokens[0].equals("exit") && !tokens[0].equals("inventory") && !tokens[0].equals("list")) {
	        		  /*---------------RECEIVE---------------*/
	        		  DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	        		  udpSocket.receive(receivePacket);
	        		  byte[] data = new byte[receivePacket.getLength()];
				      System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), data, 0, receivePacket.getLength());
	        		  String serverMessage = new String(data);
	        		  
	        		  System.out.println("FROM SERVER:" + serverMessage);			//TODO replace this line
	        		  writer.println(serverMessage);
	        		  
        		  }else if(tokens[0].equals("exit")){
        			  tcpSocket.close();
        			  udpSocket.close();
        			  writer.close();
        		  }else {
        			  //handle inventory return string
        			  DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	        		  udpSocket.receive(receivePacket);
	        		  byte[] data = new byte[receivePacket.getLength()];
				      System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), data, 0, receivePacket.getLength());
	        		 
				      String serverMessage = new String(data);
	        		  String[] inventory = serverMessage.split(";");
        			  String res = inventory[0];
        			  for(int i = 1; i < inventory.length; i++) res += "\n" + inventory[i];
        			  
	        		  System.out.println("FROM SERVER:" + res);			//TODO replace this line
	        		  writer.println(res);
        		  }
        	  }
        	  else {
        		  /*----------------SEND------------------*/
        		  outToServer.writeBytes(sentence + '\n');
        		  
        		  if(!tokens[0].equals("exit") && !tokens[0].equals("inventory") && !tokens[0].equals("list")) {
        			  /*---------------RECEIVE----------------*/ 
        			  String serverMessage = inFromServer.readLine();
        			  System.out.println("FROM SERVER: " + serverMessage);			//TODO replace this line
        			  writer.println(serverMessage);
        		  }else if(tokens[0].equals("exit")){
        			  tcpSocket.close();
        			  udpSocket.close();
        			  writer.close();
        		  }else {
        			  //handle inventory return string
        			  String serverMessage = inFromServer.readLine();
        			  String[] inventory = serverMessage.split(";");
        			  String res = inventory[0];
        			  for(int i = 1; i < inventory.length; i++) res += "\n" + inventory[i];
        				  
        			  System.out.println("FROM SERVER: " + res);			//TODO replace this line
        			  writer.println(res);
        		  }
        	  }
          }  else {
            System.out.println("ERROR: No such command");
          }
        }
    } catch (Exception e) {
    	e.printStackTrace();
    }
  }
  
  
  
}
