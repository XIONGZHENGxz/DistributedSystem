import java.util.*;
import java.io.*;
import java.net.*;

public class BookServer {
	static HashMap<String, Integer> inventory = new HashMap<String, Integer>();
	static HashMap<Integer, Record> records = new HashMap<Integer, Record>();
	static HashMap<String, Set<Integer>> studentRecordIDs = new HashMap<String, Set<Integer>>();
	static int counter = 1;

  public static void main (String[] args) {
    int tcpPort;
    int udpPort;
    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    String fileName = args[0];
    tcpPort = 7000;
    udpPort = 8000;

		try {
			Scanner invScanner = new Scanner(new File(fileName));
			while(invScanner.hasNextLine()) {
				String book = invScanner.nextLine();
				int index = book.indexOf('\"', 1);
				int stock = Integer.valueOf(book.substring(index + 2));
				book = book.substring(0, index + 1);
				inventory.put(book, stock);
			}
			invScanner.close();
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}

    // TODO: handle request from clients
		new Thread(new UDPListener(udpPort)).start();
		new Thread(new TCPListener(tcpPort)).start();
  }

	synchronized String borrow(String student, String book) {
		Integer stock = inventory.get(book);

		if(stock == null) {
			return "Request Failed - We do not have this book.";
		}
		if(stock == 0) {
			return "Request Failed - Book not available.";
		}

		int recordID = counter;
		counter++;
		inventory.put(book, stock - 1);
		Record record = new Record(student, book);
		records.put(recordID, record);
		Set recordIDs = studentRecordIDs.get(student);
		if(recordIDs == null) {
			recordIDs = new HashSet<Integer>();
		}
		recordIDs.add(recordID);
		studentRecordIDs.put(student, recordIDs);

		return "You request has been approved, " + recordID + " " + student + " " + book;
	}

	synchronized String returnBook(int recordID) {
		Record record = records.get(recordID);

		if(record == null) {
			return recordID + " not found, no such borrow record";
		}

		int stock = inventory.get(record.book);
		inventory.put(record.book, stock + 1);
		records.remove(recordID);
		Set<Integer> recordIDs = studentRecordIDs.get(record.student);
		recordIDs.remove(recordID);
		studentRecordIDs.put(record.student, recordIDs);

		return recordID + " is returned";
	}

	synchronized String list(String student) {
		Set<Integer> recordIDs = studentRecordIDs.get(student);

		if(recordIDs == null) {
			return "No record found for " + student;
		}

		String response = "";
		for(Integer recordID: recordIDs) {
			Record record = records.get(recordID);
			response += recordID + " " + record.book + "\n";
		}

		if(response.equals("")) {
			return "No record found for " + student;
		}
		return response.substring(0, response.length() - 1);
	}

	synchronized String getInventory() {
		String response = "";
		for(Map.Entry<String, Integer> book : inventory.entrySet()) {
			response += book.getKey() + " " + book.getValue() + "\n";
		}
		return response.substring(0, response.length() - 1);
	}
}

class Record {
	String student;
	String book;

	Record(String student, String book) {
		this.student = student;
		this.book = book;
	}
}

class UDPListener implements Runnable {
	DatagramSocket datasocket;
	DatagramPacket datapacket, returnpacket;

	UDPListener(int udpPort) {
		try {
			datasocket = new DatagramSocket(udpPort);
		}
		catch(SocketException e) {
			System.err.println(e);
		}
	}

	public void run() {
		byte[] buf = new byte[1024];

		try {
			while(true) {
				datapacket = new DatagramPacket(buf, buf.length);
				datasocket.receive(datapacket);
				new Thread(new UDPHandler(datapacket, datasocket)).start();
			}
		}
		catch(SocketException e) {
			System.err.println(e);
		}
		catch(IOException e) {
			System.err.println(e);
		}
	}
}

class TCPListener implements Runnable {
	ServerSocket listener;
	Socket s;

	TCPListener(int tcpPort) {
		try {
			listener = new ServerSocket(tcpPort);
		}
		catch(IOException e) {
			System.err.println(e);
		}
	}

	public void run() {
		try {
			while((s = listener.accept()) != null) {
				new Thread(new TCPHandler(s)).start();				
			}
		}
		catch(IOException e) {
			System.err.println(e);
		}
	}
}

class UDPHandler implements Runnable {
	BookServer server;
	DatagramPacket datapacket, returnpacket;
	DatagramSocket datasocket;

	UDPHandler(DatagramPacket datapacket, DatagramSocket datasocket) {
		this.datapacket = datapacket;
		this.datasocket = datasocket;
		server = new BookServer();
	}

	public void run() {
		try {
			String cmd = new String(datapacket.getData(), "UTF-8");
			int newline = cmd.indexOf("\n");
			int length = Integer.valueOf(cmd.substring(0, newline));
			cmd = cmd.substring(newline + 1, newline + length + 1);
			String response = null;
			int space1 = cmd.indexOf(" ");
			String function;
			if(space1 == -1) {
				function = cmd;
			}
			else {
				function = cmd.substring(0, space1);
			}

			if(function.equals("borrow")) {
				int space2 = cmd.indexOf(" ", space1 + 1);
				String student = cmd.substring(space1 + 1, space2);
				String book = cmd.substring(space2 + 1);
				response = server.borrow(student, book);
			}
			else if(function.equals("return")) {
				int recordID = Integer.valueOf(cmd.substring(space1 + 1));
				response = server.returnBook(recordID);
			}
			else if(function.equals("list")) {
				String student = cmd.substring(space1 + 1);
				response = server.list(student);
			}
			else if(function.equals("inventory")) {
				response = server.getInventory();
			}
			else if(function.equals("exit")) {
				PrintWriter writer = new PrintWriter("inventory.txt", "UTF-8");
				writer.println(server.getInventory());
				writer.close();
			}

			if(response != null) {
				byte[] sData = response.getBytes("UTF-8");
				returnpacket = new DatagramPacket(
						sData, 
						sData.length, 
						datapacket.getAddress(),
						datapacket.getPort());
				datasocket.send(returnpacket);
			}
		}
		catch(SocketException e) {
			System.err.println(e);
		}
		catch(IOException e) {
			System.err.println(e);
		}
	}
}

class TCPHandler implements Runnable {
	BookServer server;
	Socket s;

	TCPHandler(Socket s) {
		this.s = s;
		server = new BookServer();
	}

	public void run() {
		try {
			Scanner sc = new Scanner(s.getInputStream());
			PrintWriter pout = new PrintWriter(s.getOutputStream());
			while(true) {
				String cmd = sc.nextLine();
				String response = null;
				int space1 = cmd.indexOf(" ");
				String function;
				if(space1 == -1) {
					function = cmd;
				}
				else {
					function = cmd.substring(0, space1);
				}

				if(function.equals("borrow")) {
					int space2 = cmd.indexOf(" ", space1 + 1);
					String student = cmd.substring(space1 + 1, space2);
					String book = cmd.substring(space2 + 1);
					response = server.borrow(student, book);
				}
				else if(function.equals("return")) {
					int recordID = Integer.valueOf(cmd.substring(space1 + 1));
					response = server.returnBook(recordID);
				}
				else if(function.equals("list")) {
					String student = cmd.substring(space1 + 1);
					response = server.list(student);
				}
				else if(function.equals("inventory")) {
					response = server.getInventory();
				}
				else if(function.equals("exit")) {
					PrintWriter writer = new PrintWriter("inventory.txt", "UTF-8");
					writer.print(server.getInventory());
					writer.close();
					pout.println(0);
					pout.flush();
					break;
				}
				
				int lines = 1;
				for(int i = 0; i < response.length(); i++) {
					char c = response.charAt(i);
					if(c == '\n') {
						lines++;
					}
				}
				pout.println(lines);
				pout.println(response);
				pout.flush();
			}
			sc.close();
			pout.flush();
			s.close();
		}
		catch(IOException e) {
			System.err.println(e);
		}
	}
}
