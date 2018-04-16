/* Mustafa Irfan (mi4467)
 * Abraham Kim (adk882)
 * 
 */
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BookServer {
	static DatagramSocket dataSocket;
	static ServerSocket server;
	
	static ArrayList<String> books;
	static HashMap<String, Integer> inv;
	static HashMap<String, HashMap<Integer, String>> currently_borrowed_list_by_student;
	static HashMap<Integer, String[]> currently_borrowed_list_by_record_id;
	
	static AtomicInteger record_id;
	
	public static void main (String[] args) throws IOException {
	    int tcpPort;
	    int udpPort;
	    
	    books = new ArrayList<String>();
	    inv = new HashMap<String, Integer>();
	    currently_borrowed_list_by_student = new HashMap<String, HashMap<Integer, String>>();
	    currently_borrowed_list_by_record_id = new HashMap<Integer, String[]>();
	    record_id = new AtomicInteger(0);
	    
	    if (args.length != 1) {
	      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
	      System.exit(-1);
	    }
	    String fileName = args[0];
	    tcpPort = 7000;
	    udpPort = 8000;
	    
	    dataSocket = new DatagramSocket(udpPort);
		server = new ServerSocket(tcpPort);
	
	    // parse the inventory file
		Scanner sc = new Scanner(new FileReader(fileName));
		while(sc.hasNext()){
			StringTokenizer st = new StringTokenizer(sc.nextLine());
			int tokens = st.countTokens();
			String title = st.nextToken();
			for(int x = 1; x<tokens - 1; x++){
				title += " " + st.nextToken();
			}
			int number_of_books = Integer.parseInt(st.nextToken());
			books.add(title);
			inv.put(title, number_of_books);
		}
		
	    // TODO: handle request from clients
		
		BookServer book_server = new BookServer();
		
		new Thread(book_server.new UDPserver()).start();
		new Thread(book_server.new TCPserver()).start();
	}
	
	public class UDPClientHandler implements Runnable {
		DatagramSocket udpSocket;
		byte[] receiveData = new byte[1024];
    	byte[] sendData = new byte[1024];
		InetAddress cAdd;
		int cPort;
		int len = 1024;
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		DatagramPacket returnPacket = new DatagramPacket(sendData, sendData.length, cAdd, cPort);
		
		public UDPClientHandler(DatagramSocket ds, int clientPort, InetAddress clientAdd) {
			udpSocket = ds;
			cAdd = clientAdd;
			cPort = clientPort;
			
		}
		
		@Override
		public void run() {
			try {
				sendData = new byte[1024];
				returnPacket = new DatagramPacket(sendData, sendData.length, cAdd, cPort);
				sendData = "Connected...".getBytes();
				udpSocket.send(returnPacket);
				while(true){
					receiveData = new byte[1024];
					receivePacket = new DatagramPacket(receiveData, receiveData.length);
					udpSocket.receive(receivePacket);
					String cmd = new String(receivePacket.getData()).trim();
					String tokens[] = cmd.split(" ");
					
					if(tokens[0].equals("setmode")){
						// Set_Mode
						break;
					} else if (tokens[0].equals("borrow")) {
			            // Borrow
						String student = tokens[1];
						String book = tokens[2];
						for(int x = 3; x<tokens.length; x++){
							book += " " + tokens[x];
						}
						if(borrow(book)){
							int rec = record_id.incrementAndGet();
							String[] hold = new String[]{student, book};
							currently_borrowed_list_by_record_id.put(rec, hold);
							modify_borrow_list(rec, student, book);
							sendData = new byte[1024];
							sendData = ("Your request has been approved, " + rec + " " + student + " " + book).getBytes();
							returnPacket =  new DatagramPacket(sendData, sendData.length, cAdd, cPort);
							udpSocket.send(returnPacket);
						}
						else{
							sendData = new byte[1024];
							sendData = ("Request Failed - Book not available").getBytes();
							returnPacket =  new DatagramPacket(sendData, sendData.length, cAdd, cPort);
							udpSocket.send(returnPacket);
						}
						
			        } else if (tokens[0].equals("return")) {
			            // Return
			        	int rec_id = Integer.parseInt(tokens[1]);
			        	if(return_book(rec_id)){
			        		sendData = new byte[1024];
			        		sendData = (rec_id + " is returned").getBytes();
			        		returnPacket =  new DatagramPacket(sendData, sendData.length, cAdd, cPort);
							udpSocket.send(returnPacket);
			        	}
			        	else{
			        		sendData = new byte[1024];
			        		sendData = (rec_id + " not found, no such borrow record").getBytes();
			        		returnPacket =  new DatagramPacket(sendData, sendData.length, cAdd, cPort);
							udpSocket.send(returnPacket);
			        	}
			        } else if (tokens[0].equals("inventory")) {
			            // Inventory
			        	synchronized(inv){
				            for(int x = 0; x<inv.size(); x++){
				            	sendData = new byte[1024];
				            	sendData = ("" + books.get(x) + " " + inv.get(books.get(x))).getBytes();
				            	returnPacket =  new DatagramPacket(sendData, sendData.length, cAdd, cPort);
								udpSocket.send(returnPacket);
				            }
				            sendData = new byte[1024];
				            sendData = ("done").getBytes();
				            returnPacket =  new DatagramPacket(sendData, sendData.length, cAdd, cPort);
							udpSocket.send(returnPacket);
			        	}
			        } else if (tokens[0].equals("list")) {
			            // List
			        	String student = tokens[1];
			        	if(!currently_borrowed_list_by_student.containsKey(student)){
			        		sendData = new byte[1024];
			        		sendData = ("No record found for " + student).getBytes();
			        		returnPacket =  new DatagramPacket(sendData, sendData.length, cAdd, cPort);
							udpSocket.send(returnPacket);
							sendData = new byte[1024];
			        		sendData = ("done").getBytes();
			        		returnPacket =  new DatagramPacket(sendData, sendData.length, cAdd, cPort);
							udpSocket.send(returnPacket);
			        	}
			        	else{
				        	synchronized(currently_borrowed_list_by_student.get(student)){
				        		if(currently_borrowed_list_by_student.get(student).isEmpty()){
				        			sendData = new byte[1024];
					        		sendData = ("No record found for " + student).getBytes();
					        		returnPacket =  new DatagramPacket(sendData, sendData.length, cAdd, cPort);
									udpSocket.send(returnPacket);
									sendData = new byte[1024];
					        		sendData = ("done").getBytes();
					        		returnPacket =  new DatagramPacket(sendData, sendData.length, cAdd, cPort);
									udpSocket.send(returnPacket);
				        		}
				        		else{
				        			Integer[] rec_ids = currently_borrowed_list_by_student.get(student).keySet().toArray(new Integer[0]);
				        			Arrays.sort(rec_ids);
				        			for(int x = 0; x<currently_borrowed_list_by_student.get(student).size(); x++){
				        				sendData = new byte[1024];
				        				sendData = (rec_ids[x].toString() + " " + currently_borrowed_list_by_student.get(student).get(rec_ids[x])).getBytes();
				        				returnPacket =  new DatagramPacket(sendData, sendData.length, cAdd, cPort);
										udpSocket.send(returnPacket);
				        			}
				        			sendData = new byte[1024];
				        			sendData = ("done").getBytes();
				        			returnPacket =  new DatagramPacket(sendData, sendData.length, cAdd, cPort);
									udpSocket.send(returnPacket);
				        		}
				        	}
			        	}
			        } else if (tokens[0].equals("exit")) {
			            // Exit
			        	outputInventory();
			        	break;
			        } else {
			            System.out.println("ERROR: No such command");
			        }
				}
				udpSocket.close();
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public class UDPserver implements Runnable {
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		@Override
		public void run() {
			while(true){
				try {
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					dataSocket.receive(receivePacket);
					DatagramSocket udpSocket = new DatagramSocket();
					int clientPort = receivePacket.getPort();
					InetAddress clientAdd = receivePacket.getAddress();
					Runnable t = new UDPClientHandler(udpSocket, clientPort, clientAdd);
					new Thread(t).start();
				} 
				catch(SocketException e){
					System.err.println(e);
				} 
				catch(IOException e){
					System.err.println(e);
				}
			}
		}
		
	}

	public class TCPserver implements Runnable {		//the idea here is to create a handler for each client, since it is 
		PrintWriter middleman;
		Socket  socket;
		@Override
		public void run() {
			try {				
				while(true) {
					socket = server.accept();
					ServerSocket connect = new ServerSocket(0);
					int port = connect.getLocalPort();				
					middleman = new PrintWriter(socket.getOutputStream());
					middleman.println(port);
					middleman.flush();
					Thread t = new Thread(new TCPClientHandler(connect));
					t.start();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
	
	public class TCPClientHandler implements Runnable {
		String client;
		PrintWriter writer;
		BufferedReader reader;
		Socket socket;
		ServerSocket server;
		
		public TCPClientHandler(ServerSocket s) {
			server = s;
		}
		
		@Override
		public void run() {
			String cmd;
			boolean flag = false;
			try { 
				socket = server.accept();
				writer = new PrintWriter(socket.getOutputStream());
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				while(true) {
					cmd = reader.readLine();
			        String[] tokens = cmd.split(" ");
					if(tokens[0].equals("setmode")){
						// Set_Mode
						break;
					}  else if (tokens[0].equals("borrow")) {
			            // Borrow
						String student = tokens[1];
						String book = tokens[2];
						for(int x = 3; x<tokens.length; x++){
							book += " " + tokens[x];
						}
						if(borrow(book)){
							int rec = record_id.incrementAndGet();
							String[] hold = new String[]{student, book};
							currently_borrowed_list_by_record_id.put(rec, hold);
							modify_borrow_list(rec, student, book);
							writer.println("Your request has been approved, " + rec + " " + student + " " + book);
							writer.flush();
						}
						else{
							writer.println("Request Failed - Book not available");
							writer.flush();
						}
			        } else if (tokens[0].equals("return")) {
			            // Return
			        	int rec_id = Integer.parseInt(tokens[1]);
			        	if(return_book(rec_id)){
			        		writer.println(rec_id + " is returned");
			        		writer.flush();
			        	}
			        	else{
			        		writer.println(rec_id + " not found, no such borrow record");
			        		writer.flush();
			        	}
			        } else if (tokens[0].equals("inventory")) {
			        	// Inventory
			        	synchronized(inv){
				            for(int x = 0; x<inv.size(); x++){
				            	writer.println("" + books.get(x) + " " + inv.get(books.get(x)));
				            	writer.flush();
				            }
				            writer.println("done");
				            writer.flush();
			        	}
			        } else if (tokens[0].equals("list")) {
			            // List
			        	String student = tokens[1];
			        	if(!currently_borrowed_list_by_student.containsKey(student)){
			        		writer.println("No record found for " + student);
			        		writer.flush();
			        		writer.println("done");
		        			writer.flush();
			        	}
			        	else {
				        	synchronized(currently_borrowed_list_by_student.get(student)){
				        		if(currently_borrowed_list_by_student.get(student).isEmpty()){
				        			writer.println("No record found " + student);
				        			writer.flush();
				        			writer.println("done");
				        			writer.flush();
				        		}
				        		else{
				        			Integer[] rec_ids = currently_borrowed_list_by_student.get(student).keySet().toArray(new Integer[0]);
				        			Arrays.sort(rec_ids);
				        			for(int x = 0; x<currently_borrowed_list_by_student.get(student).size(); x++){
				        				writer.println(rec_ids[x].toString() + " " + currently_borrowed_list_by_student.get(student).get(rec_ids[x]));
				        				writer.flush();
				        			}
				        			writer.println("done");
				        			writer.flush();
				        		}
				        	}
			        	}
			        	
			        } else if (tokens[0].equals("exit")) {
			            // Exit
			        	outputInventory();
			        	break;
			        } else {
			            System.out.println("ERROR: No such command");
			        }
				}
				socket.close();
				server.close();
			}
			catch(Exception e) {
				
			}			
		}
		
		
		
	}
	private void outputInventory() throws FileNotFoundException, UnsupportedEncodingException{
		synchronized(inv){
			PrintWriter outToFile = new PrintWriter("inventory.txt");
        	for(int x = 0; x<books.size(); x++){
        		outToFile.println(books.get(x) + " " + inv.get(books.get(x)));
        	}
        	outToFile.close();
		}
	}
	
	private boolean borrow(String book){
		if(!inv.containsKey(book)){
			return false;
		}
		boolean result = true;
		synchronized(inv.get(book)){
			if(inv.get(book) == 0){
				result = false;
			}
			else{
				inv.replace(book, inv.get(book) - 1);
				result =  true;
			}
		}
		return result;
	}
	
	private boolean return_book(int rec_id){
		if(!currently_borrowed_list_by_record_id.containsKey(rec_id)){
			return false;
		}
		String book = "";
		synchronized(currently_borrowed_list_by_record_id.get(rec_id)){
			if(currently_borrowed_list_by_record_id.get(rec_id)[0].equals("")){
				return false;
			}
			book = currently_borrowed_list_by_record_id.get(rec_id)[1];
		}
		synchronized(inv.get(book)){
			inv.replace(book, inv.get(book) + 1);
		}
		modify_return_list(rec_id);
		return true;
	}
	
	private void modify_borrow_list(int rec_id, String student, String book){
		synchronized(currently_borrowed_list_by_student){
			if(!currently_borrowed_list_by_student.containsKey(student)){
				HashMap <Integer, String> hold = new HashMap<Integer, String>();
				currently_borrowed_list_by_student.put(student, hold);
			}
		}
		synchronized(currently_borrowed_list_by_student.get(student)){
			currently_borrowed_list_by_student.get(student).put(rec_id, book);
		}
	}
	
	private void modify_return_list(int rec_id){
		String student = currently_borrowed_list_by_record_id.get(rec_id)[0];
		String[] hold = new String[]{"",""};
		currently_borrowed_list_by_record_id.replace(rec_id, hold);
		synchronized(currently_borrowed_list_by_student.get(student)){
			currently_borrowed_list_by_student.get(student).remove(rec_id);
		}
	}
  
}