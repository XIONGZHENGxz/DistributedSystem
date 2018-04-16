
/*
 * UTEIDs: csf596, cfd363
 */

import java.io.*;
import java.net.*;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BookServer{

	private static ConcurrentHashMap<String, Integer> inventory; //maps book names to inventory counts
	private static ConcurrentHashMap<Integer, Record> records; //maps record numbers to Records
	private static int tcpPort;
	protected static int udpPort;
	private static AtomicInteger nextRecord = new AtomicInteger(0);
	protected static ServerSocket tcpSocket;
	
	public static void main(String[] args) {

		
		if (args.length != 1) { 
			System.out.println("ERROR: Provide 1 argument: input file containing initial inventory"); 
			System.exit(-1);
		} 
		String fileName = args[0];
		tcpPort = 7000;
		udpPort = 8000;

		inventory = new ConcurrentHashMap<String, Integer>();
		records = new ConcurrentHashMap<Integer, Record>();

		// parse the inventory file
		try {
			Scanner sc = new Scanner(new FileReader(fileName));
			while (sc.hasNextLine()) {
				String cmd = sc.nextLine();
				String[] tokens = cmd.split("\" ");
				addBook(tokens[0]+"\"", Integer.parseInt(tokens[1]));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			tcpSocket = new ServerSocket(tcpPort); //initialize TCP socket
		} catch (Exception e) {
			e.printStackTrace();
		}
		(new UDPListener()).start();
		(new TCPListener()).start();
	}
	
	public static void addBook(String name, int num) { // pre: inventory non-null
		Integer prevNum = inventory.get(name);
		if (prevNum != null) {
			inventory.put(name, prevNum + num);
		} else {
			inventory.put(name, num);
		}
	}
	
	public synchronized static String handleCommand(String command) {
		String[] tokens = command.split(" ");
		System.out.println("\nCommand: "+command);
		String response = null;
		switch (tokens[0]) {
			case "borrow":
				String bookName = command.substring(command.indexOf("\""));
				response = handleBorrowRequest(tokens[1], bookName);
				break;
			case "return":
				response = handleReturnRequest(Integer.parseInt(tokens[1]));
				break;
			case "list":
				response = handleListRequest(tokens[1]);
				break;
			case "inventory":
				response = handleInventoryRequest();
				break;
			case "exit":
				handleExitRequest();
				break;
		}
		return response;
	}
	
	
	public static String handleBorrowRequest(String studentName, String bookName) {
		if (!inventory.containsKey(bookName)) {
			System.out.println("Could not find "+bookName);
			return "Request Failed - We do not have this book";
		}
		else if (inventory.get(bookName).intValue() == 0) {
			System.out.println(bookName+" is unavailable.");
			return "Request Failed - Book not available";
		} else {
			int count = inventory.get(bookName).intValue();
			inventory.put(bookName, count-1); //decrement inventory count
			int recNum = nextRecord.addAndGet(1);
			records.put(recNum, new Record(bookName, studentName));
			System.out.println("Request has been approved for <"+studentName+", "+bookName+">");
			return "Your request has been approved, "+recNum+" "+studentName+" "+bookName;
		}
	}
	
	public static String handleReturnRequest(int recordID) {
		Record record = records.get(recordID);
		if (record == null) {
			System.out.println("Client tried to return record "+recordID+", but no such record exists.");
			return recordID+" not found, no such borrow record";
		} else {
			inventory.put(record.bookName, inventory.get(record.bookName).intValue() + 1); //increment
			records.remove(recordID); // Should we do this ?
			System.out.println("Record "+recordID+" ("+record.bookName+") has been returned.");
			return recordID+" is returned";
		}
	}
	
	public static String handleListRequest(String studentName) {
		String list = "";
		for (Entry<Integer, Record> e : records.entrySet()) {
			if (e.getValue().studentName.equals(studentName)) {
				list+= e.getKey().intValue() + " " + e.getValue().bookName+"\n";
			}
		}
		if (list.length() == 0) {
			System.out.println("No record found for "+studentName);
			return "No record found for "+studentName;
		}else {
			System.out.print(list);
			return list;
		}
	}
	
	public static String handleInventoryRequest() {
		String inv = "";
		for (Entry<String, Integer> e : inventory.entrySet()) {
			inv += e.getKey()+" "+e.getValue()+"\n";
		}
		System.out.print(inv);
		return inv;
	}
	
	public static void handleExitRequest() {
		// print inventory to file and stop processing client
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("inventory.txt"));
    		for (Entry<String, Integer> e : inventory.entrySet()) {
        		writer.write(e.getKey()+" "+e.getValue());
        		writer.newLine();
    		}
    		writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

//Wrapper for record data
class Record {
	protected String bookName;
	protected String studentName;
	
	public Record(String bookName, String studentName) {
		this.bookName = bookName;
		this.studentName = studentName;
	}
}

//One server thread constantly handling all UDP packets
class UDPListener extends Thread {
	public void run() {
		try {
			DatagramSocket socket = new DatagramSocket(BookServer.udpPort); //socket for all UDP
			boolean running = true;
			System.out.println("Server has begun listening to UDP requests.");
			while (running) {
				//handle UDP stuff
				byte[] buffer = new byte[256];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet); //block until you receive packet
				String command = new String(packet.getData(), 0, packet.getLength());
				
				String response = BookServer.handleCommand(command);
				if (response != null) {
					buffer = response.getBytes();
					packet = new DatagramPacket(buffer, buffer.length, packet.getSocketAddress());
					//System.out.println("Sending UDP Response: "+response);
					socket.send(packet);
				}
			}
			socket.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

//One server thread constantly accepting new TCP socket connections
class TCPListener extends Thread {
	public void run() {
		try {
			boolean running = true;
			System.out.println("Server is open for accepting incoming TCP connections.");
			while (running) {
				//handle incoming TCP connections
				Socket s = BookServer.tcpSocket.accept();
				ClientThread ct = new ClientThread(s, new DataInputStream(s.getInputStream()), new DataOutputStream(s.getOutputStream()));
				ct.start();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

//A thread for each client using TCP
class ClientThread extends Thread {
	private Scanner scan = new Scanner(System.in);
	private final DataInputStream input;
	private final DataOutputStream output;
	Socket socket;
	
	public ClientThread(Socket socket, DataInputStream input, DataOutputStream output) {
		this.socket = socket;
		this.input = input;
		this.output = output;
	}
	
	public void run() {
		boolean running = true;
		while (running) {
			try {
				String command = input.readUTF();
				String response = BookServer.handleCommand(command);
				if (response != null) {
					//System.out.println("Sending TCP Response: "+response);
					output.writeUTF(response);
				}
			} catch (Exception e) {
				//connection closed / reset
				running = false;
			}
		}
	}
}