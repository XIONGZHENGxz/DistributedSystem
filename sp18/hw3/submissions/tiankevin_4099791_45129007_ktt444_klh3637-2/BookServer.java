/**
 * HW3 EE360P
 * Authors: Kevin Tian ktt444
 * 			Kenneth Hall klh3637
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class BookServer{
	
	static class Record {
		int id;
		String bookname;
		
		Record(){}
		Record(int id){this.id = id;}
	}
	
	
	static HashMap<String, Integer> inventory;	//String - Bookname; Integer - Num of Copies left
    static HashMap<String, ArrayList<Record>> recordIds;  //String - student names; ArrayList - All records w/ student
    static AtomicInteger idNum;	//to make new record numbers
    static ArrayList<String> order;
    static int tcpPort;
    static int udpPort;
    static int newtcpPort; 
    static int newudpPort;
    
  public static void main (String[] args) {

    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    
    idNum = new AtomicInteger(0);
    tcpPort = 9000;
    udpPort = 8080;
    inventory = new HashMap<String, Integer>();
    recordIds = new HashMap<String, ArrayList<Record>>();

    
    //TODO parse inventory file
    try {
        String fileName = args[0];	//this contains the library
    	Scanner sc = new Scanner(new FileReader(fileName));
    	order = new ArrayList<String>();
    	
	    while(sc.hasNextLine()) {
	    	String line = sc.nextLine();
	    	String[] tokens = line.split(" ");
	    	
	    	String name = tokens[0];
	    	for(int i = 1; i < tokens.length-1; i++) name += " " + tokens[i];
	    	int quantity = Integer.parseInt(tokens[tokens.length-1]);
	    	
	    	inventory.put(name, quantity);
	    	order.add(name);
	    	System.out.println(name + " " + quantity);
	    }
	} catch (FileNotFoundException e1) {
		e1.printStackTrace();
	}


    // TODO: handle request from clients
    try {
    	
		ServerSocket welcomeSocket = new ServerSocket(tcpPort);
		DatagramSocket serverSocket = new DatagramSocket(udpPort);
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		
		while (true) {
			
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	    	serverSocket.receive(receivePacket); 
	    	int port = receivePacket.getPort();
	    	String initString = new String(receivePacket.getData());
	    	String[] tokens = initString.split(" ");
	    	
			if(tokens[1].equals("initial")) {	
			
				newtcpPort = 0;
			    newudpPort = 0;
				
				Runnable myRunnable = new Runnable(){
				     public void run(){
				    	
				    	try {
				    	
					    ServerSocket welcomeSock = new ServerSocket(0);
					    DatagramSocket serverSock = new DatagramSocket(0);
					    newtcpPort = welcomeSock.getLocalPort();
					    newudpPort = serverSock.getLocalPort();
					    System.out.println("ports altered " + newtcpPort + " " + newudpPort);
					    	
				    	Socket connectionSocket = welcomeSock.accept();
				 		byte[] receiveData = new byte[1024];
						byte[] sendData = new byte[1024];
						boolean udp = true;
				    	String result;
				    	 	while(true) {
					    	 if(udp) {
						    	 /*---------------------UDP-----------------------*/
									
						    	 DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
						    	 serverSock.receive(receivePacket);
						    	 byte[] data = new byte[receivePacket.getLength()];
						    	 System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), data, 0, receivePacket.getLength());
						    	 
						    	 
						    	 String udpString = new String(data);
						    	 System.out.println("Received: " + udpString);
						    	 int port = receivePacket.getPort();
						    	 
						    	 result = parse(udpString);	//parse sentence
						    	 
						    	 if(result.equals("change")) udp = false;
						    	 else if(result.equals("exit\n")) {
						    		 welcomeSock.close();
						    		 serverSock.close();
						    		 return;
						    	 }
						    	 else {
						    		 sendData = result.getBytes();
						    		 DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), port);
						    		 serverSock.send(sendPacket);
						    		 System.out.println("passed UDP");
						    	 }

					    	 }else {
						    	 /*---------------------TCP-----------------------*/
								
							    	 BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
							    	 DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
							    	 
							    	 String tcpString = inFromClient.readLine();
							    	 System.out.println("Received: " + tcpString);
							    	 result = parse(tcpString) + '\n';	//parse sentence
							    	 
							    	 if(result.equals("change\n")) udp = true;
							    	 else if(result.equals("exit\n")) {
							    		 welcomeSock.close();
							    		 serverSock.close();
							    		 return;
							    	 }
							    	 else {
							    		 outToClient.writeBytes(result);
							    		 System.out.println("passed TCP");
							    	 }
						     }
				    	 }
				    	 	
				    	 }catch(Exception e){
				    		 e.printStackTrace();
				    	 }
				    }
				};

				Thread thread = new Thread(myRunnable);
				thread.start();
				
				System.out.println(" " + newtcpPort + " " + newudpPort + " ");
				
				while(newtcpPort == 0 || newudpPort == 0) {
					System.out.println("inside");
				}
				sendData = (" " + newtcpPort + " " + newudpPort + " ").getBytes();

	    		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), port);
	    		serverSocket.send(sendPacket);
			}
			
	
		}	
		
    }catch(Exception e) {
    	e.printStackTrace();
    }
  }
 
  
  public synchronized static String parse(String input) throws Exception {	//TODO implement this method
	  
	  PrintWriter writer = new PrintWriter("inventory.txt", "UTF-8");
	  String result = "";
	  String[] tokens = input.split(" ");
	  

	  
	  /*change from udp to tcp*/
	  if(tokens[0].equals("tcpchange")) {	
		  result = "change";
	  }
	  /*change from tcp to udp*/
	  else if(tokens[0].equals("udpchange")) {	
		  result = "change";
	  }
	  
	  else if(tokens[0].equals("borrow")) {	//token[1]=studentname	token[2]=bookname
		  
		  /*Check if book is available*/
		  String name = tokens[2];
		  for(int i = 3; i < tokens.length; i++) {
			  name += " " + tokens[i];
		  }
		  
		  if(inventory.containsKey(name)) {
			  if(inventory.get(name) == 0) result = "Request Failed - Book not available";
			  else {
				  /*Book is available. Make record, decrement count*/
				  int id = idNum.incrementAndGet();
				  result = "Your request has been approved, " + id + " " + tokens[1] + " " + name;
				  Record newRec = new Record(id);
				  newRec.bookname = name;
				  
				  /*Checks if student has records. If true, add to existing. If false, make an arraylist*/
				  if(recordIds.containsKey(tokens[1])) recordIds.get(tokens[1]).add(newRec);
				  else {
					  ArrayList<Record> records = new ArrayList<Record>();
					  records.add(newRec);
					  recordIds.put(tokens[1], records);
				  }	//record added
				  
				  /*Decrement count in library*/
				  inventory.put(name, inventory.get(name)-1);
			  }
		  }else {
			  result = "Request Failed - We do not have this book";
		  }
		  
		  
	  }
	  else if(tokens[0].equals("return")) {
		  boolean found = false;
		  ArrayList<Record> person = null;
		  Record target = null;
		  
		  for(ArrayList<Record> recordList : recordIds.values()) {
			  for(Record record : recordList) {
				  if(record.id == Integer.parseInt(tokens[1])) {
					  found = true;
					  person = recordList;
					  target = record;
					  /*increment count in library*/
					  inventory.put(record.bookname, inventory.get(record.bookname) + 1);
				  }
			  }
		  }
		  if(!found) result = tokens[1] + " not found, no such borrow record";
		  else {
			  person.remove(target);
			  result = tokens[1] + " is returned";
		  }
	  }
	  else if(tokens[0].equals("list")) {
		  /*check if record is there. If true, output arrayList*/
		  if(recordIds.containsKey(tokens[1])) {
			  int count = 0;
			  for(Record record : recordIds.get(tokens[1])) {
				  if(count == 0) result += record.id + " " + record.bookname;
				  else result += ";" + record.id + " " + record.bookname;
				  count++;
			  }
		  }else result = "No record found for " + tokens[1];
	  }
	  else if(tokens[0].equals("inventory")) {
		  int count = 0;
		  for(String name : order) {
			  if(count == 0) result += name + " " + inventory.get(name);
			  else result += ";" + name + " " + inventory.get(name);
			  count++;
		  }
	  }
	  else if(tokens[0].equals("exit")) {
		  int count = 0;
		  for(String name : order) {
			  if(count == 0) result += name + " " + inventory.get(name);
			  else result += "\n" + name + " " + inventory.get(name);
			  count++;
		  }
		  writer.print(result);
		  writer.close();
		  result = "exit";
	  }
	  
	  return result;
  }
   
  
  
  
}
