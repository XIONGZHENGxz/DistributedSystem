// Jia-luen Yang (jy8435)
// Kevin Pham (kmp2996)

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookServer{
	
  public static void main (String[] args) {
	  int tcpPort;
	  int udpPort;
	  if (args.length != 1) {
		  System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
		  System.exit(-1);
	  }
	  String fileName = args[0];
	  tcpPort = 7001;
	  udpPort = 8001;
    
	  // parse the inventory file
	  try {
		  Scanner sc = new Scanner(new FileReader(fileName));

		  while(sc.hasNextLine()) {
			  String cmd = sc.nextLine();
			  
			  List<String> list = new ArrayList<String>();
			  Matcher matcher = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(cmd);
			  while (matcher.find()) {
				  list.add(matcher.group(1));
			  }
			  String[] tokens = list.toArray(new String[list.size()]);			  
			
			  String bookName = tokens[0];
			  int bookQuantity = Integer.parseInt(tokens[1]);
			  			  
			  library.put(bookName, bookQuantity);
			  booksOrder.add(bookName);
		  }
	  } catch (FileNotFoundException e) {
    	  e.printStackTrace();
	  }
    
	  // TODO: handle request from clients
	  
	  // UDP + TCP Listen
	   
	   TCPListen tcpListen = new TCPListen(tcpPort);
	   UDPListen udpListen = new UDPListen(udpPort);
	   tcpListen.start();
	   udpListen.start();
	  
//	  try {
//		  
//		  DatagramSocket udpSock = null;
//		  udpSock = new DatagramSocket(udpPort);
//		  
//		  ServerSocket tcpListener = null;
//		  tcpListener = new ServerSocket(tcpPort);
//		  
//		  while(true) {
//			  
//			  UDPThread udpT = new UDPThread(udpSock);		
//			  udpT.run();
//
//			  Socket tcpSock = null;
//			  tcpSock = tcpListener.accept();
//		      TCPThread tcpT = new TCPThread(tcpSock);
//			  tcpT.run();
//		  }
//		  
//	  } catch (IOException e) {
//		  // TODO Auto-generated catch block
//		  e.printStackTrace();
//	  }
	  
  }
  

  
  static class Record {
	int recordID;
	String username, bookName;
	
	public Record (int id, String user, String book) {
		this.recordID = id;
		this.username = user;
		this.bookName = book;
	}
  }
  //library is all books available and number of book
  private static HashMap<String, Integer> library = new HashMap<String, Integer>();
  //records is for records of orders
  private static HashMap<Integer, Record> records = new HashMap<Integer, Record>();
  private static int numRecord = 1;
  
  private static ArrayList<String> booksOrder = new ArrayList<String>();
  
  public static String parseCommand (String command) {
	  System.out.println(command);
	  List<String> list = new ArrayList<String>();
	  Matcher matcher = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(command);
	  while (matcher.find()) {
		  list.add(matcher.group(1));
	  }
	  String[] tokens = list.toArray(new String[list.size()]);	
	  
	  
    switch (tokens[0]) {
      case "borrow": return borrowBook(tokens[1], tokens[2]);
      case "return": return returnBook(tokens[1]);
      case "list": return list(tokens[1]);
      case "inventory": return inventory();
      case "exit": return null;
    }
    
	return "error";
  }
  
  public synchronized static String borrowBook(String student, String book) {
	Integer i = library.get(book);
	if (i == null) {
		return "Request Failed - We do not have this book";
	}
	else if(library.get(book) == 0) {
	  return "Request Failed - Book not available";
	} else {
	  library.put(book, i - 1);
	  Record record = new Record(numRecord, student, book);
	  numRecord++;
	  records.put(record.recordID, record);
	  return "You request has been approved, " + record.recordID + " " + record.username + " " + record.bookName;
	}  
  }
  
  public synchronized static String returnBook(String recordId) {
	Record record = records.remove(Integer.parseInt(recordId)); // Might be wrong because the records shouldn't be deleted?
	if(record == null) {
		return recordId + " not found, no such borrow record";
	}
	library.put(record.bookName, library.get(record.bookName) + 1);
	return recordId + " is returned";
  }
  
  public synchronized static String list(String student) {
	String list = "";
	String nextLine = "";
	for(Record record: records.values()) {
	  if(record.username.equals(student)) {
		list += nextLine + record.recordID + " " + record.bookName;
		nextLine = "\n";
	  }
	}
	if(list.equals("")) {
		return "No record found for " + student;
	}
	return list;
  }
  
  public synchronized static String inventory() {
	String list = "";
	String nextLine = "";
	
	// Has to list the books in the same order received in the inventory.txt
	for(String book: booksOrder) {
		list += nextLine + book + " " + library.get(book);
		nextLine = "\n";
	}
	
//	for(Map.Entry<String, Integer> book : library.entrySet()) {
//		list += nextLine + book.getKey() + " " + book.getValue();
//		nextLine = "\n";
//	}

	return list;
  }
}


class TCPListen extends Thread{
	
	int tcpPort;
	
	public TCPListen(int tcpPort) {
		this.tcpPort = tcpPort;
	}
	
	public void run() {
		try {	  
			  System.out.println("Listening to TCP");
			  ServerSocket tcpListener = null;
			  tcpListener = new ServerSocket(tcpPort);
			  
			  while(true) {
				  Socket tcpSock = null;
				  tcpSock = tcpListener.accept();
			      TCPThread tcpT = new TCPThread(tcpSock);
				  tcpT.run();
			  }
			  
		  } catch (IOException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }
	}
}

class UDPListen extends Thread{
	
	int udpPort;
	
	public UDPListen(int udpPort) {
		this.udpPort = udpPort;
	}
	
	public void run() {
		try {	  
			  System.out.println("Listening for UDP");
			  DatagramSocket udpSock = null;
			  udpSock = new DatagramSocket(udpPort);
			  
			  while(true) {
				  UDPThread udpT = new UDPThread(udpSock);
				  udpT.run();
				  
			  }
			  
		  } catch (IOException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }
	}
}


class TCPThread extends Thread{
	  
	  Socket socket = null;
	  
	  public TCPThread(Socket socket){
	      this.socket = socket;
	  }
	  
	  public void run() {
		  try {
			  // System.out.println("TCP Thread");
			  DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			  BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));      
			  
		      String line;
		      while ((line = in.readLine()) != null) {
		    	  String str = BookServer.parseCommand(line);
		    	  // System.out.println(str);
		    	  
		    	  if (line.equals("exit")) {
		    	      PrintWriter inv = new PrintWriter("inventory.txt");
		    	      String[] invout = BookServer.inventory().split("\n");
		    	      for(String s: invout) {
		    	    	  inv.println(s);
		    	      }
		    	      inv.close();
		    	      socket.close();
		    	      return;
		    	  }
		    	  
		    	  String[] outs = str.split("\n");
		    	  out.writeBytes(Integer.toString(outs.length) + "\n");
		    	  // System.out.println("Length: " + outs.length);
		    	  
		    	  for(String s: outs) {
		    		  if (!s.equals(""))
		    			  out.writeBytes(s + "\n");
		    		  else
		    			  System.out.println("bug");
		    	  }
		    	  
		    	  
		    	  // break;
		      }	
		      // socket.close();
		      
		  } catch (IOException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }
		  
	  }
}


class UDPThread extends Thread{
	DatagramSocket socket;
	
	public UDPThread(DatagramSocket socket) {
		this.socket = socket;
	}
	
	public void run() {
		// System.out.println("UDP Thread");
		
        try {
        	byte[] receiveData = new byte[1024];
            byte[] sendData = new byte[1024];
    		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			socket.receive(receivePacket);
			String command = new String(receivePacket.getData(), 0 , receivePacket.getLength());
			
			if (command.equals("exit")) {
	    	      PrintWriter inv = new PrintWriter("inventory.txt");
	    	      String[] invout = BookServer.inventory().split("\n");
	    	      for(String s: invout) {
	    	    	  inv.println(s);
	    	      }
	    	      inv.close();
	    	      // socket.close();
	    	      return;
	    	  }
			
			
			String str = BookServer.parseCommand(command);
			
			sendData = str.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());
            socket.send(sendPacket);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
