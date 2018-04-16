import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BookServer{
	ReentrantLock invLock;
	ReentrantLock studLock;
	ReentrantLock idLock;
	ReentrantLock libLock;
	HashMap<String, Integer> inventory;
	HashMap<Integer, ClientThread> clients;
	HashMap<String, ArrayList<Integer>> students;
	HashMap<Integer, String> libStatus;
	DatagramSocket udpIn;
	int recordId;
	
	public static void main (String[] args) {
		 if (args.length != 1) {
		 	System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
		 	System.exit(-1);
		 }		
		BookServer server = new BookServer(parse(args[0]));
		new Thread(server.new UdpListener()).start();
	}
	
	public BookServer(HashMap<String, Integer> inventory) {
		this.inventory = inventory;
		invLock = new ReentrantLock();
		studLock = new ReentrantLock();
		idLock = new ReentrantLock();
		libLock = new ReentrantLock();
		students = new HashMap<String, ArrayList<Integer>>();
		libStatus = new HashMap<Integer, String>();
		recordId = 1;
		try {
			udpIn = new DatagramSocket(8000);
		} catch (SocketException e) {
			System.out.println("Server socket setup exception");
		}
		clients = new HashMap<Integer, ClientThread>();
	}
	
	public boolean hasInventory(String title) {
		invLock.lock();
		boolean ret =  inventory.containsKey(title);
		invLock.unlock();
		return ret;
	}
	
	public int getInventory(String title) {
		invLock.lock();
		int copies = inventory.get(title);
		invLock.unlock();
		return copies;
	}
	
	public void putInventory(String title, int copies) {
		invLock.lock();
		inventory.put(title, copies);
		invLock.unlock();
	}
	
	public boolean hasName(String name){
		studLock.lock();
		boolean ret = students.containsKey(name);
		studLock.unlock();
		return ret;
	}
	
	public void addNewStudent(String name, ArrayList<Integer> recordIds) {
		studLock.lock();
		students.put(name, recordIds);
		studLock.unlock();
	}
	
	public void updateRecordId() {
		idLock.lock();
		recordId++;
		idLock.unlock();
	}

	public int getRecordId() {
		idLock.lock();
		int ret = recordId;
		idLock.unlock();
		return ret;
	}
	
	public void updateStudentRecord(String name, int id) {
		studLock.lock();
		students.get(name).add(id);
		studLock.unlock();
	}
	
	public void removeFromStudents(int recId) {
		studLock.lock();
		for (String name : students.keySet()) {
			ArrayList<Integer> books = students.get(name);
			if (books.contains(recId)) {
				books.remove(Integer.valueOf(recId));
			}
		}
		studLock.unlock();
	}
	
	public void addToLibStatus(int recId, String title) {
		libLock.lock();
		libStatus.put(recId, title);
		libLock.unlock();
	}
	
	public void removeFromLibStatus(int recId) {
		libLock.lock();
		libStatus.remove(recId);
		libLock.unlock();
	}
	
	public boolean libStatusContains(int recId){
		libLock.lock();
		boolean ret = libStatus.keySet().contains(recId);
		libLock.unlock();
		return ret;
	}
	
	public String getLibStatus(int recId) {
		libLock.lock();
		String ret = libStatus.get(recId);
		libLock.unlock();
		return ret;
	}
	
	public String list(String name) {
		studLock.lock();
		libLock.lock();
		String msg = "";
		ArrayList<Integer> bookIds = students.get(name);
		for (int i : bookIds) {
			msg = msg + i + " " + libStatus.get(i) + "\n";
		}
		if(msg.length() > 0)
			msg = msg.substring(0, msg.length() - 1);
		else
			msg = "No record found for " + name;		
		libLock.unlock();
		studLock.unlock();
		
		return msg;
	}
	
	public String getInventory() {
		invLock.lock();
		String msg = "";
		for (String title : inventory.keySet()) {
			msg = msg + title + " " + inventory.get(title) + "\n";
		}
		msg = msg.substring(0, msg.length()-1);
		invLock.unlock();
		return msg;
	}
	
	public BookServer getOuter() {
		return this;
	}
	
	class UdpListener implements Runnable{
		@Override
		public void run(){
			while(true) {
				byte[] buf = new byte[1024];
				DatagramPacket datapacket = new DatagramPacket(buf, buf.length);
				try {
					udpIn.receive(datapacket);
				} catch (IOException e) {
					System.out.println("Client error receiving message");
				}
				String cmd = new String(datapacket.getData(), 0, datapacket.getLength());
				int clientId = Integer.parseInt(cmd.substring(0, 1));
				if(!clients.containsKey(clientId)) {
					ClientThread newClient = new ClientThread(clientId, getOuter());
					newClient.addCmd(cmd);
					clients.put(clientId, newClient);
				} else {
					clients.get(clientId).addCmd(cmd);
				}
			}
		}
	}

	public static HashMap<String, Integer> parse(String file){
		HashMap<String, Integer> map = new LinkedHashMap<String,Integer>();
		Scanner sc = null;
		try{
		 	sc = new Scanner(new FileReader("input/input.txt"));
		} catch (Exception e){
			System.out.println(e.getMessage());

		}

	 	while(sc.hasNextLine()) {
	 		String line = sc.nextLine();
	 		String[] tokens = line.split(" ");
	 		int num = Integer.parseInt(tokens[tokens.length-1]);

	 		//populate string title
	 		String title = "";
	 		for (int i = 0; i < tokens.length-1; i++){
	 			title = title + tokens[i] + " ";
	 		}

	 		//remove extra space
	 		title = title.substring(0, title.length() - 1);

	 		//put title and num into hashmap
	 		map.put(title, num);
	 	}
		return map;
	}
}

class ClientThread{
	int clientId;
	DatagramSocket udpOut;
	BookServer bookServer;
	Queue<String> cmds;
	Scanner clientIn;
	PrintStream clientOut;
	static ServerSocket serverSocket;
	private final ReentrantLock qLock;
	private final Condition qEmpty;
	boolean tcpConnected;
	boolean exit;
	boolean udpMode;
	
	public ClientThread(int clientId, BookServer bs) {
		this.clientId = clientId;
		bookServer = bs;
		cmds = new LinkedList<String>();
		qLock = new ReentrantLock();
		qEmpty = qLock.newCondition();
		tcpConnected = false;
		exit = false;
		udpMode = true;
		try {
			udpOut = new DatagramSocket();
			if(serverSocket == null)
				serverSocket = new ServerSocket(7000);
		} catch (Exception e) {
			System.out.println("Server setup exception");
		}
		new Thread(new Service()).start();
	}
	
	public void addCmd(String cmd) {
		qLock.lock();
			cmds.add(cmd);
			if(cmds.size() == 1)
				qEmpty.signal();
		qLock.unlock();
	}
	
	public void sendMessage(String msg) {
		try {
			if(udpMode)
				sendUdpMessage(msg);
			else
				sendTcpMessage(msg);
		} catch (Exception e) {
			System.out.println("Server send message exception");
		}
	}
	
	private void sendUdpMessage(String msg) throws Exception {
		byte[] buf = new byte[msg.length()];
		buf = msg.getBytes();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(), 8000+clientId);
		udpOut.send(packet);
	}
	
	private void sendTcpMessage(String msg) {
		clientOut.println(msg);
		clientOut.flush();
	}
	
	public void borrow(String[] tokens){
		//get student name and book title
		String name = tokens[2];
		String title = "";
		for (int i = 3; i < tokens.length; i++) {
			title = title + tokens[i] + " ";
		}
		title = title.substring(0, title.length() - 1);

		//see if book is available and update inventory if necessary
		if (!bookServer.hasInventory(title)) {
			sendMessage("Request Failed - We do not have this book");
			return;
		}
		int copies = bookServer.getInventory(title);
		if (copies == 0) {
			sendMessage("Request Failed - Book not available");
			return;
		}
		bookServer.putInventory(title, copies-1);

		//check if student exists in database
		if (!bookServer.hasName(name)) {
			ArrayList<Integer> recordIds = new ArrayList<Integer>();
			bookServer.addNewStudent(name, recordIds);
		}
		
		//update student record and library log record
		int id = bookServer.getRecordId();
		bookServer.updateRecordId(); //for next person
		bookServer.updateStudentRecord(name, id);
		
		bookServer.addToLibStatus(id, title);
		sendMessage("Your request has been approved, " + Integer.toString(id) + " " + 
						name + " " + title);
	}
	
	public void returnBook(String[] tokens) {
		int recId = Integer.parseInt(tokens[2]);
		if (!bookServer.libStatusContains(recId)) {
			sendMessage(Integer.toString(recId) + " not found, no such borrow record");
			return;
		}
		
		//increment inventory
		String title = bookServer.getLibStatus(recId);
		int num = bookServer.getInventory(title);
		bookServer.putInventory(title, num + 1);
		
		//remove from lib log and student log
		bookServer.removeFromLibStatus(recId);
		bookServer.removeFromStudents(recId);
		
		sendMessage(Integer.toString(recId) + " is returned");
		
		//signal that inventory has been incremented
		
	}
	
	public void list(String[] tokens) {
		if (!bookServer.hasName(tokens[2]))
			sendMessage("No record found for " + tokens[2]);
		else
			sendMessage(bookServer.list(tokens[2]));		
	}
	
	public void inventory(String[] tokens) {
		String output = bookServer.getInventory();
		String[] msgs = output.split("\n");
		sendMessage(Integer.toString(msgs.length));
		for(String msg: msgs){
			sendMessage(msg);
		}
	}
	
	@SuppressWarnings("resource")
	public void setMode(String[] tokens) {
		sendMessage("Wait");
		if(tokens[2].equals("T")) {
			if(!tcpConnected) {
				try {
					new Thread(new ClientTcp(serverSocket.accept())).start();
				} catch (IOException e) {
					System.out.println("Server tcp socket creation exception");
					System.out.println(e.getMessage());
				}
				tcpConnected = true;
			}
			udpMode = false;
		}
		else{
			udpMode = true;
		}
	}
	
	public void exit() {
		String output = bookServer.getInventory();
		
		//write output to inventory.txt
		try {
			PrintWriter out = new PrintWriter(new File("inventory.txt"));
		    out.print(output);
		    out.flush();
		    out.close();
		} catch (Exception e) {
			System.out.println("error outputting inventory to file at exit");
		}
		exit = true;
		udpOut.close();
		
	}
	
	class Service implements Runnable{
		@Override
		public void run() {
			while(!exit) {
				qLock.lock();
					while(cmds.isEmpty()) {
						try {
							qEmpty.await();
						} catch (InterruptedException e) {
							System.out.println("Interrupted exception");
						}
					}
					String cmd = cmds.remove();
				qLock.unlock();
				
				//service here
				String[] tokens = cmd.split(" ");
				if (tokens[1].equals("borrow")) {
					borrow(tokens);
				} else if (tokens[1].equals("return")) {
					returnBook(tokens);
				} else if (tokens[1].equals("list")) {
					list(tokens);
				} else if (tokens[1].equals("inventory")) {
					inventory(tokens);
				} else if (tokens[1].equals("setmode")) {
					setMode(tokens);
				} else {		//exit
					exit();
				}
			}
		}
		
	}
	
	class ClientTcp implements Runnable{
		
		public ClientTcp(Socket client) {
			try {
				clientIn = new Scanner(client.getInputStream());
				clientOut = new PrintStream(client.getOutputStream());
			} catch (IOException e) {
				System.out.println("tcpIn/Out initialization exception");
			}
		}

		@Override
		public void run() {
			while(!exit) {
				try{
					addCmd(Integer.toString(clientId) + " " + clientIn.nextLine());
				}
				catch(Exception e){}
			}
			clientIn.close();
			clientOut.close();
		}
	}
}




