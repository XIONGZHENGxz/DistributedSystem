/*
 * Gilad Croll - GC24654
 * Andoni Mendoza - AM46882
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class BookServer{
	static LinkedHashMap <String, Integer> inventoryMap = new LinkedHashMap<String, Integer>(); 
	static Map<String, ArrayList<BookRecord>> studentToBookMap = new HashMap<String, ArrayList<BookRecord>>();	// map student to list of books he has
	static Semaphore mutex = new Semaphore(1);	// mutex object to ensure only one thread touching the inventory maps
	static int recordIDCount = 0;
	public static void main (String[] args) throws InterruptedException {
		int tcpPort;
		int udpPort;		
		if (args.length != 1) {
			System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
			System.exit(-1);
		}

		String fileName = args[0];
		tcpPort = 7000;
		udpPort = 8000;
		int len = 1024;


		// parse the inventory file and store it in the inventory hashmap
		try {
			mutex.acquire();
			File file = new File(fileName);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String bookName = line.substring(0,line.lastIndexOf('"')+1);
				Integer count =  Integer.parseInt(line.substring(line.lastIndexOf('"')+2,line.length()));
				inventoryMap.put(bookName,count);
				// TODO question: can one student borrow a book more than once at a time? 2 copies of the same book..
			}
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		mutex.release();
		// handle request from clients

		new Thread(new UDPHandler(udpPort)).start();
		new Thread(new TCPHandler(tcpPort)).start();
	}

	// the "main" thread to invoke all other incoming UDP requests and their threads
	static class UDPHandler implements Runnable{
		int udpPort;
		int len = 1024;
		DatagramPacket datapacket;

		public UDPHandler(int udpPort){
			this.udpPort = udpPort;
		}

		@Override
		public void run() {
			try {
				DatagramSocket datasocket = new DatagramSocket(udpPort);
				byte[] buf = new byte[len];
				while (true) {
					datapacket = new DatagramPacket(buf, buf.length);
					datasocket.receive(datapacket);
					new Thread(new UDPWorker(datasocket, datapacket)).start();				
				}
			} catch (Exception e) {System.err.println(e);}
		}
	}

	// the "main" thread to invoke all other incoming TCP requests and their threads
	static class TCPHandler implements Runnable{
		int tcpPort;
		int len = 1024;
		ServerSocket serverSocket = null;

		public TCPHandler(int tcpPort){
			this.tcpPort = tcpPort;
		}

		@Override
		public void run() {			
			try {
				this.serverSocket = new ServerSocket(this.tcpPort);
			} catch (IOException e1) {e1.printStackTrace();}

			while(true){
				Socket clientSocket = null;
				try {
					clientSocket = this.serverSocket.accept();
				} catch (IOException e) {throw new RuntimeException("Error accepting client connection", e);}		        
				new Thread(new TCPWorker(clientSocket)).start();		        
			}

		}
	}
	
	static class TCPWorker implements Runnable {	
		protected Socket clientSocket = null;
	    	   	
		public TCPWorker(Socket clientSocket){
		    this.clientSocket = clientSocket;
		}

		@Override
		public void run() {
			try {
				Scanner sc = new Scanner(clientSocket.getInputStream());
				OutputStream  pout = clientSocket.getOutputStream();
				String cmd = sc.nextLine();
				pout.write(getResponse(cmd).getBytes());
				sc.close();
				pout.flush();
				clientSocket.close();
			} catch (IOException | InterruptedException e) {e.printStackTrace();}			
		}				
	}

	synchronized static public String getResponse(String cmd) throws InterruptedException, FileNotFoundException, UnsupportedEncodingException{		
		try{
			mutex.acquire();
			String[] tokens = cmd.split(" ");
			if (tokens[0].equals("borrow")) {
				String studentName = tokens[1];
				String bookName = cmd.substring(cmd.indexOf('"'), cmd.length());
				int numOfCopies = inventoryMap.get(bookName);
				if (numOfCopies == 0){	// check if any copies left
					return "Request Failed - Book not available";
				}
				else{	// takes this else if it book is available
					inventoryMap.put(bookName, numOfCopies-1);	// decrement count of book in inventory
					recordIDCount++;
					BookRecord newBook = new BookRecord(recordIDCount, bookName);
					if (!studentToBookMap.containsKey(studentName))
						studentToBookMap.put(studentName, new ArrayList<BookRecord>());
					studentToBookMap.get(studentName).add(newBook);
					return "Your request has been approved, " + recordIDCount + " " + studentName + " " + bookName;
				}				
			}
			else if (tokens[0].equals("return")) {
				int recordIDtoRemove = Integer.parseInt(tokens[1]);
				for (String key : studentToBookMap.keySet()) {	// iterate over each student
					for (BookRecord b: studentToBookMap.get(key)){	// iterate over each book record for specific student
						if (b.recordID == recordIDtoRemove){
							inventoryMap.put(b.bookName, inventoryMap.get(b.bookName)+1);	// increment count of book in inventory
							studentToBookMap.get(key).remove(b);	//remove from student to book map
							return recordIDtoRemove + " is returned";							
						}
					}
				}
				return recordIDtoRemove + " not found, no such borrow record"; 
			}
			else if (tokens[0].equals("list")) {
				String studentName = tokens[1];
				String ret = "";	// String to return
				if (!studentToBookMap.containsKey(studentName) || studentToBookMap.get(studentName).isEmpty()){
					return "No record found for <student-name>";
				}
				else{
					boolean first = true;
					for (BookRecord b:studentToBookMap.get(studentName)){
						if (first){
							first = false;
							ret += b.recordID+ " " + b.bookName;
						}
						else
							ret += "\n" + b.recordID+ " " + b.bookName;
					}
					return ret;
				}
			}
			else if (tokens[0].equals("inventory")) {
				boolean firstLine = true;
				String ret = "";	// String to return
				for (String key : inventoryMap.keySet()){
					if (firstLine){
						firstLine = false;
						ret += key + " " + inventoryMap.get(key);
					}else
						ret += "\n" + key + " " + inventoryMap.get(key);
				}
				return ret;
			}
			else if (tokens[0].equals("exit")) {
				PrintWriter writer = new PrintWriter("inventory.txt", "UTF-8");															
				for (String key : inventoryMap.keySet())
					writer.println(key + " " + inventoryMap.get(key));								
				writer.close();
				return "";
			}
			else{
				return "unrecognized command";
			}
		}finally{mutex.release();}		
	}

	static class BookRecord{
		int recordID;
		String bookName;

		public BookRecord(int recordID, String bookName){
			this.recordID = recordID;
			this.bookName = bookName;
		}
	}

	static class UDPWorker extends Thread {
		DatagramPacket datapacket; // packet used for receiving
		DatagramSocket datasocket;
		DatagramPacket sPacket;	// packet used for sending

		public UDPWorker(DatagramSocket socket, DatagramPacket packet){
			this.datasocket = socket;
			this.datapacket = packet;
		}

		@Override
		public void run() {
			String cmd = new String(datapacket.getData(), 0, datapacket.getLength());			
			try {
				String ret = getResponse(cmd);
				if (!ret.equals(""))
					sendDG(ret);
			} catch (InterruptedException | FileNotFoundException | UnsupportedEncodingException e) {e.printStackTrace();}			
		}

		// sends udp datagram with a given parameter string
		void sendDG(String cmd){
			byte[] buffer = new byte[cmd.length()];
			buffer = cmd.getBytes();
			sPacket = new DatagramPacket(buffer, buffer.length, datapacket.getAddress(), datapacket.getPort());
			try {
				datasocket.send(sPacket);
			} catch (IOException e) {e.printStackTrace();}	
		}
	}
}
