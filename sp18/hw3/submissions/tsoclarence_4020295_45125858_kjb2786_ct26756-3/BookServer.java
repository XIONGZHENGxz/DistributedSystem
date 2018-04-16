import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.Semaphore;


public class BookServer {
   static Queue<String> clientQueue = new LinkedList<String>();
   static Semaphore queueLock = new Semaphore(1);
   
   static LinkedHashMap<String, Integer> inventory = new LinkedHashMap<String, Integer>();
   static Semaphore inventoryLock = new Semaphore(1);
   
   static LinkedHashMap<Integer, Transaction> transactions = new LinkedHashMap<Integer, Transaction>();
   static Semaphore transactionsLock = new Semaphore(1);
   
   static LinkedHashMap<String, ArrayList<Integer>> studentList = new LinkedHashMap<String,ArrayList<Integer>>();
   static Semaphore studentListLock = new Semaphore(1);

   static int recordID =1 ;
   static Semaphore recordIDLock = new Semaphore(1);
   
   


  public static void main (String[] args) {
	BookServer bookserver = new BookServer();
    int tcpPort;
    int udpPort;
    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    String fileName = args[0];
    tcpPort = 7000;
    udpPort = 8000;

    String input = null;
    
    try {
		FileReader fileReader = new FileReader(fileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		
		while((input = bufferedReader.readLine()) != null){
			
			//CHECK FOR QUOTATION MARKS
			String[] split = input.split("\" ");
			split[0] += "\"";
			int split1 = Integer.parseInt(split[1]);
			
			inventory.put(split[0], split1);
		}
		
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    TCPServer tcpserver = new TCPServer();
    Thread tcpThread = new Thread(tcpserver);
    tcpThread.start();
    // parse the inventory file

    // TODO: handle request from clients
    //UDP
    DatagramPacket datapacket;
    try {
		DatagramSocket datasocket = new DatagramSocket(8000);
		byte[] buf = new byte[1024];
		while(true){
			datapacket = new DatagramPacket(buf, buf.length);
			datasocket.receive(datapacket);
			String receivedString = new String(datapacket.getData());
			int returnport = datapacket.getPort();
			InetAddress returnaddress = datapacket.getAddress();
			/*
			queueLock.acquire();
				clientQueue.add(receivedString);
			queueLock.release();
			*/
			
			ParseUDP fuck = new ParseUDP(receivedString, returnaddress, returnport);
			Thread t = new Thread(fuck);
			t.start();
			
		}
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
  
  

  
static class TCPServer implements Runnable{
	  ServerSocket serverSocket;
	  Socket socket;
	  Scanner in;
	  Thread bookshelfInstance;
	  
	 
	  
		@Override
		public void run() {
			try {
				serverSocket = new ServerSocket(7000);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			while(true){
				
					try {
					socket = serverSocket.accept();
									
//					in = new Scanner(socket.getInputStream());
//					String message= in.nextLine();

//					queueLock.acquire();
//						clientQueue.add(message);
//					queueLock.release();
					
					ParseTCP tcpFuck = new ParseTCP(socket);
					Thread t = new Thread(tcpFuck);
					t.start();
					
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				
			}
			
		}
		
	}

	public synchronized static String printCheckedOut(String student) throws InterruptedException{
		studentListLock.acquire();
		transactionsLock.acquire();

		String reply = "";
		ArrayList<Integer> checkedOut = studentList.get(student);
		int count = 0;
		
		if(checkedOut == null || checkedOut.isEmpty()){
			reply = "No record found for " + student + "\n"; 
			count =1;
		}
		else{
			for(int i : checkedOut){
				reply += Integer.toString(i) + " " + transactions.get(i).book + "\n";
				
				count++;
				
			}
			
		}
		transactionsLock.release();
		studentListLock.release();
		reply = reply.substring(0, reply.length() -1);
		return Integer.toString(count) +"\n" + reply;
	}
	public static String printInventory() throws InterruptedException{
		inventoryLock.acquire();
		String reply = "";
		int count = 0;
		for(String book : inventory.keySet()){
			reply += (book + " " + inventory.get(book).toString() + "\n");
			count++;
		}
		inventoryLock.release();
		reply = reply.substring(0, reply.length() -1);
		return Integer.toString(count) + "\n" + reply;
	}
	
	public static void incrementRecordID() throws InterruptedException{
		recordIDLock.acquire();
			recordID ++;
		recordIDLock.release();

	}
	
	public static void addTransaction(int recordID, Transaction trans) throws InterruptedException{
		transactionsLock.acquire();
			studentListLock.acquire();
			transactions.put(recordID, trans);
			if(studentList.get(trans.student) == null){
				ArrayList<Integer> checkedOut = new ArrayList<Integer>();
				checkedOut.add(recordID);
				studentList.put(trans.student, checkedOut);
			}
			else{
				ArrayList<Integer> checkedOut = studentList.get(trans.student);
				checkedOut.add(recordID);
				studentList.put(trans.student, checkedOut);
				
			}
			incrementRecordID();
			studentListLock.release();
		transactionsLock.release();
		
	}

	public  static int borrowBook(Transaction trans) throws InterruptedException{
		inventoryLock.acquire();
			if(inventory.get(trans.book) == null){
				inventoryLock.release();	

				return -1;
				
			}
			
			else if(inventory.get(trans.book) == 0){
				inventoryLock.release();	

				return -2;
			}
			else{
				
				int num = inventory.get(trans.book);
				num --;
				inventory.put(trans.book, num);
				int currentID = recordID;
				addTransaction(recordID, trans);
				inventoryLock.release();	

				return currentID;
			}
			
	}
	
	public static int returnBook(Transaction trans, int transID) throws InterruptedException{
		inventoryLock.acquire();
		    //"\\"The Letter\\"
			String title = trans.book;
			if(inventory.get(title) == null){
				inventoryLock.release();	
				return -1;
				
			}
						
			else{
				int num = inventory.get(title);
				num ++;
				inventory.put(trans.book, num);
				inventoryLock.release();	

				studentListLock.acquire();
				ArrayList<Integer> checkedOut = studentList.get(trans.student);
				
				for(int i = 0; i < checkedOut.size(); i++){
					if(checkedOut.get(i) == transID){
						checkedOut.remove(i);
						break;
					}
				}
				studentList.put(trans.student, checkedOut);
				studentListLock.release();

				return 1;
			}
			
	}
  
  
}






