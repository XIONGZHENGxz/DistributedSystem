/* Mustafa Irfan (mi4467)
 * Abraham Kim (adk882)
 * 
 */
import java.util.Scanner;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.*;

public class BookClient {
	public static void main (String[] args) {
	    String hostAddress;
	    int tcpPort;
	    int udpPort;
	    int clientId;
	    String currentMode = "U";
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
	    	/*Basic Inits*/
	    	File output = new File("out_" + clientId + ".txt");			//output file
	    	PrintWriter outWriter = new PrintWriter(output);
	    	
	        Scanner sc = new Scanner(new FileReader(commandFile));
	    	//Scanner sc = new Scanner(System.in);
	       
	        InetAddress IPAddress = InetAddress.getByName(hostAddress);
	        int connectedPort = -1; //Only Necessary for UDP connections
	        Socket tcpClient = new Socket();
		    PrintWriter writer =null;
		    BufferedReader reader = null;
		   
	        /*UDP Default Init*/
	        DatagramSocket udpClient = new DatagramSocket();
	        connectedPort = udpSetup(udpClient, udpPort, IPAddress);
	        while(sc.hasNextLine()) {
	          String cmd = sc.nextLine();
	          String[] tokens = cmd.split(" ");	
	          if (tokens[0].equals("setmode")) {
	        	  if(currentMode.equals("U")){
	        		  if(tokens[1].equals("T")){
	            		  currentMode = "T";	            		  
	            		  /*Closing udpSocket*/
	            		  byte[] send = new byte[1024];
	            		  send = ("setmode").getBytes();
	            		  DatagramPacket sendPacket = new DatagramPacket(send, send.length, IPAddress, udpPort);
	            		  udpClient.send(sendPacket);
	            		  udpClient.close();
	            		  
	            		  /*Start tcpSocket*/
	            		 
	            		  tcpClient = new Socket(IPAddress, tcpPort);
	            		  Socket tcpClientHold = tcpSetup(tcpClient, IPAddress, reader, writer);
	            		  tcpClient.close();
	            		  tcpClient = tcpClientHold;
	            		  reader = new BufferedReader(new InputStreamReader(tcpClient.getInputStream()));
	            		  writer = new PrintWriter(tcpClient.getOutputStream());
	            	  }
	        	  }
	        	  else if(currentMode.equals("T")){
		        	  if(tokens[1].equals("U")){
		        		  currentMode = "U";
		        		  
		        		  /*Closing tcpSocket*/
		        	      writer = new PrintWriter(tcpClient.getOutputStream());
		        		  writer.println("setmode");
		        		  writer.flush();
		        	      tcpClient.close();
		        		  
		        		  /*Start udpSocket*/
		        		  udpClient = new DatagramSocket();
		        		  connectedPort = udpSetup(udpClient, udpPort, IPAddress);
		        	  }
	        	  }
	          }
	          else if (tokens[0].equals("borrow")) {
	        	  if(currentMode.equals("T")) {
	        		  borrowBook(cmd, writer, reader, outWriter);
	        	  }
	        	  else {
	        		  borrowBook(udpClient, connectedPort, cmd, IPAddress, outWriter);
	        	  }
	          } else if (tokens[0].equals("return")) {
	        	  if(currentMode.equals("T")) {
	        		  returnBook(cmd, writer, reader, outWriter);
	        	  }
	        	  else {
	        		  returnBook(udpClient, connectedPort, cmd, IPAddress, outWriter);
	        	  }
	          } else if (tokens[0].equals("inventory")) {
	        	  if(currentMode.equals("T")) {
	        		  listInventory(cmd, writer, reader, outWriter);
	        	  }
	        	  else {
	        		  listInventory(udpClient, connectedPort, cmd, IPAddress, outWriter);
	        	  }
	          } else if (tokens[0].equals("list")) {
	        	  if(currentMode.equals("T")) {
	        		  listRecord(cmd, writer, reader, outWriter);
	        	  }
	        	  else {
	        		  listRecord(udpClient, connectedPort, cmd, IPAddress, outWriter);
	        	  }
	          } else if (tokens[0].equals("exit")) {
	        	  if(currentMode.equals("T")) {
	        		  writer.println("exit"); 	//the client handler it sends to should close the serversocket, and it should close the socket
	        		  writer.flush();
	        		  writer.close();
	        		  reader.close();
	        		  tcpClient.close();           //close socket on the end of the client
	        	  }
	        	  else {
	        		  byte[] send = new byte[1024];
	        		  send = ("exit").getBytes();
	        		  DatagramPacket command = new DatagramPacket(send, send.length, IPAddress, connectedPort);
	        		  udpClient.send(command);
	        		  udpClient.close();
	        	  }    	  
	          } else {
	            System.out.println("ERROR: No such command");
	          }
	        }
	    } catch (FileNotFoundException e) {
		e.printStackTrace();
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static int udpSetup(DatagramSocket udpClient, int udpPort, InetAddress IPAddress) throws IOException{
		byte[] send = new byte[1024];
	    byte[] receive = new byte[1024];
	    send = ("Connecting...").getBytes();
	    DatagramPacket sendPacket = new DatagramPacket(send, send.length, IPAddress, udpPort);
	    udpClient.send(sendPacket);
	    DatagramPacket receivePacket = new DatagramPacket(receive, receive.length);
	    udpClient.receive(receivePacket);
	    return receivePacket.getPort();
	}	
	
	private static Socket tcpSetup(Socket tcpClient, InetAddress IPAddress, BufferedReader reader, PrintWriter writer) throws IOException{
		reader = new BufferedReader(new InputStreamReader(tcpClient.getInputStream()));
	    String message = null;
	    while(message==null){
			message=reader.readLine();
	    }
	    int port = Integer.parseInt(message);
	    tcpClient.close();
	    tcpClient = new Socket(IPAddress, port);
		return tcpClient;
	}
	
	private static void borrowBook(DatagramSocket ds, int port, String cmd, InetAddress IPAddress, PrintWriter outWriter) throws IOException {
		byte[] send = new byte[1024];
	    byte[] receive = new byte[1024];
	    send = cmd.getBytes();
		DatagramPacket command = new DatagramPacket(send, send.length, IPAddress,  port);
		DatagramPacket recieved = new DatagramPacket(receive, receive.length);
		ds.send(command);
		ds.receive(recieved);
		byte[] message = recieved.getData();
		String s = new String(message);
		s = s.trim();
		outWriter.println(s + "\n");
		outWriter.flush();
		receive = new byte[1024];
		return;
	}
	
	private static void borrowBook(String cmd, PrintWriter writer, BufferedReader reader, PrintWriter outWriter) throws IOException {
		  String message=null;
		  writer.println(cmd);
		  writer.flush();
		  System.out.println("We sent in borrow: " + cmd);
		  while((message=reader.readLine())==null) {}
		  System.out.println("Got the message");
		  outWriter.println(message + "\n");
		  outWriter.flush();
    }
	
	private static void returnBook(String cmd, PrintWriter writer, BufferedReader reader, PrintWriter outWriter) throws IOException {
		  String message=null;
		  writer.println(cmd);
		  writer.flush();
		  while((message=reader.readLine())==null) {}
		  outWriter.println(message + "\n");
		  outWriter.flush();
    }
	
	private static void returnBook(DatagramSocket ds, int port, String cmd, InetAddress IPAddress, PrintWriter outWriter) throws IOException {
		byte[] send = new byte[1024];
	    byte[] receive = new byte[1024];
	    send = cmd.getBytes();
		DatagramPacket command = new DatagramPacket(send, send.length, IPAddress,  port);
		DatagramPacket recieved = new DatagramPacket(receive, receive.length);
		ds.send(command);
		ds.receive(recieved);
		byte[] message = recieved.getData();
		String s = new String(message);
		s = s.trim();
		outWriter.println(s + "\n");
		outWriter.flush();
		return;
	}

	public static void listRecord(String cmd, PrintWriter writer, BufferedReader reader, PrintWriter outWriter) throws IOException {
		  String message;
		  writer.println(cmd);
		  writer.flush();
		  boolean stillGettingRecords = true;
		  while(stillGettingRecords) {
			  while((message=reader.readLine())!=null) {
				  if(message.equals("done")) {
					  stillGettingRecords = false;
					  break;
				  }
				  else {
					  outWriter.println(message + "\n");
					  outWriter.flush();
				  }
			  }
		  }	  
	}
	
	public static void listRecord(DatagramSocket ds, int port, String cmd, InetAddress IPAddress, PrintWriter outWriter) throws IOException {
		byte[] send = new byte[1024];
	    byte[] receive = new byte[1024];
	    //Set<String> set = new LinkedHashSet<String>();
	    send = cmd.getBytes();
		DatagramPacket command = new DatagramPacket(send, send.length, IPAddress,  port);
		DatagramPacket recieved = new DatagramPacket(receive, receive.length);
		ds.send(command);
		while(true) {
			ds.receive(recieved);
			String message = new String(recieved.getData());
			message = message.trim();
			
			if(message.equals("done")) {
				break;
			}
			System.out.println("Message recieved: " + message);
			outWriter.println(message + "\n");
			outWriter.flush();
			receive = new byte[1024];
			recieved = new DatagramPacket(receive, receive.length);
		}
	}
	  
	public static void listInventory(String cmd, PrintWriter writer, BufferedReader reader, PrintWriter outWriter) throws IOException {
		  String message;
		  writer.println(cmd);
		  writer.flush();
		  boolean stillGettingInventory = true;
		  while(stillGettingInventory) {
			  while((message=reader.readLine())!=null) {
				  if(message.equals("done")) {
					  stillGettingInventory = false;
					  break;
				  }
				  else {
					  //write to output file, we need some sort of global output file
					  outWriter.println(message + "\n");
					  outWriter.flush();
				  }
			  }
		  }	  
	}
	
	public static void listInventory(DatagramSocket ds, int port, String cmd, InetAddress IPAddress, PrintWriter outWriter) throws IOException {
		byte[] send = new byte[1024];
	    byte[] receive = new byte[1024];
	    send = cmd.getBytes();
		DatagramPacket command = new DatagramPacket(send, send.length, IPAddress,  port);
		DatagramPacket recieved = new DatagramPacket(receive, receive.length);
		ds.send(command);
		while(true) {
			ds.receive(recieved);
			String message = new String(recieved.getData());
			message = message.trim();
			
			if(message.equals("done")) {
				break;
			} 
			outWriter.println(message + "\n");
			outWriter.flush();
			receive = new byte[1024];
			recieved = new DatagramPacket(receive, receive.length);
		} 
		
	}
  
}