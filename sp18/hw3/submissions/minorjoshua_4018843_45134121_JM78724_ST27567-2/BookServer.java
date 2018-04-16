import java.util.*;
import java.io.*;
import java.net.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class BookServer {
	int tcpPort;
	int udpPort;
	int recordId;

	// stores book inventory
	Map<String, Integer> books;

	// stores books checked out by given student
	Map<String, ArrayList<Record>> studentRecords;

	// stores all transaction records
	Map<Integer, String> transactions;

	// takes in a client command and returns appropriate string
	protected String handleRequest(String cmd) {
		String[] tokens = cmd.split(" ");

		if (tokens[0].equals("borrow")) {
			Pattern p = Pattern.compile("\"([^\"]*)\"");
			Matcher m = p.matcher(cmd);
			m.find();
			return this.borrow(tokens[1], m.group(1));
		} else if (tokens[0].equals("return")) {
			return this.bookReturn(Integer.parseInt(tokens[1]));
		} else if (tokens[0].equals("inventory")) {
			return this.inventory();
		} else if (tokens[0].equals("list")) {
			return this.listBorrows(tokens[1]);
		} else {
			System.out.println("ERROR: No such command: " + cmd);
			return "";
		}
	}

	public BookServer(int tcpPort, int udpPort, String inventoryFile) {
		this.books = new LinkedHashMap<String, Integer>();
		this.studentRecords = new LinkedHashMap<String, ArrayList<Record>>();
		this.transactions = new LinkedHashMap<Integer, String>();
		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
		this.recordId = 1;

		// parse the inventory file
		try {
			Scanner sc = new Scanner(new FileReader(inventoryFile));
			Pattern p = Pattern.compile("\"([^\"]*)\"");

			while(sc.hasNextLine()) {
				String input = sc.nextLine();
				Matcher m = p.matcher(input);

				int lastQuote = input.lastIndexOf("\"");

				if (lastQuote > 0) {
					m.find();
					this.books.put(m.group(1), Integer.parseInt(input.substring(lastQuote).replaceAll("[\\D]", "")));
				}
				else {
					System.out.println("ERROR: Malformed input");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// called when server receives request from client to borrow book
	public synchronized String borrow(String studentName, String bookName) {
		System.out.println("Borrowing sName: " + studentName + " bName: " + bookName);
		if(!this.books.containsKey(bookName)) {
			return "Request Failed - We do not have this book";
		}
		else if(this.books.get(bookName) == 0) {
			return "Request Failed - Book not available";
		}
		else {
			// update lib inventory
			int oldVal = this.books.get(bookName);
			this.books.replace(bookName, oldVal, oldVal - 1);

			// store transaction record
			this.transactions.put(recordId, bookName);

			// store record of book with student
			if(!this.studentRecords.containsKey(studentName)) {
				this.studentRecords.put(studentName, new ArrayList<Record>());
			}
			this.studentRecords.get(studentName).add(new Record(recordId, bookName));

			return "You request has been approved, " + new Integer(recordId++).toString() + " " + studentName + " " + wrap(bookName);
		}
	}

	// called when server receives return request
	public synchronized String bookReturn(int id) {
		// test to see if id exists in transaction history
		if (!this.transactions.containsKey(id)) {
			return new Integer(id).toString() + " not found, no such borrow record";
		} else {
			// update lib inventory
			int oldVal = this.books.get(this.transactions.get(id));
			this.books.replace(this.transactions.get(id), oldVal, oldVal + 1);

			return new Integer(id).toString() + " is returned";
		}
	}

	// called when server receives list request
	public synchronized String listBorrows(String studentName) {
		if(!this.studentRecords.containsKey(studentName)) {
			return "No record found for " + studentName + "\n";
		} else {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < this.studentRecords.get(studentName).size(); i++) {
				Record r = this.studentRecords.get(studentName).get(i);
				sb.append(new Integer(r.id).toString() + " " + wrap(r.bookName));
				if(i != this.studentRecords.get(studentName).size() - 1) {
					sb.append('\n');
				}
			}
			return sb.toString();
		}
	}

	public synchronized String inventory() {
		StringBuilder sb = new StringBuilder();
		for(String key : this.books.keySet()) {
			sb.append(wrap(key) + " " + this.books.get(key).toString() + "\n");
		}
		return sb.toString();
	}

	private String wrap(String s) {
		return "\"" + s + "\"";
	}

	public static void main (String[] args) {
		// check for input file in command line
		if (args.length != 1) {
			System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
			System.exit(-1);
		}
		String inventoryFile = args[0];

		// initialize Book server with hardcoded tcp and udp port nums
		BookServer lib = new BookServer(7000, 8000, inventoryFile);

		// initialize UDP server thread
		Runnable udpServer = () -> {
			try {
				DatagramPacket datapacket, returnpacket;
				DatagramSocket datasocket = new DatagramSocket(lib.udpPort);
				while (true) {
					byte[] receive_buf = new byte[1024];
					datapacket = new DatagramPacket(receive_buf, receive_buf.length);
					datasocket.receive(datapacket);

					// receive ID from client
					String idStr = new String(receive_buf, 0, datapacket.getLength());
					System.out.println("Received UDP Datapacket from id: " + idStr);
					int id = Integer.parseInt(idStr);

					// return client id + original udp port
					String response = new Integer(id + lib.udpPort).toString();
					byte[] send_buf = response.getBytes();

					returnpacket = new DatagramPacket(send_buf, send_buf.length,
							datapacket.getAddress(), datapacket.getPort());
					datasocket.send(returnpacket);

					// spawn new listener on the UDP port generated
					new Thread(new UdpWorker(id + lib.udpPort, id, lib)).start();
				}
			} catch (Exception e) {
				System.err.println(e);
			}
		};
		new Thread(udpServer).start();

		// start TCP listener on the main thread
		try {
			ServerSocket welcomeSocket = new ServerSocket(lib.tcpPort);

			while (true) {
				Socket connectionSocket = welcomeSocket.accept();
				System.out.println("Accepted connectionSocket: " + connectionSocket);
				new Thread(new TcpWorker(connectionSocket, lib)).start();
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	public class Record {
		int id;
		String bookName;

		public Record(int id, String bookName){
			this.id = id;
			this.bookName = bookName;
		}
	}
}

class TcpWorker implements Runnable {

	protected Socket clientSocket = null;
	protected BookServer bk;

	public TcpWorker(Socket clientSocket, BookServer bk) {
		this.clientSocket = clientSocket;
		this.bk = bk;
	}

	public void run() {
		try {
			BufferedReader inFromClient =
				new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintWriter outToClient = new PrintWriter(clientSocket.getOutputStream(),true);

			String command = inFromClient.readLine();
			System.out.println("Command from client: " + command);
			String response = this.bk.handleRequest(command);
			System.out.println("Response formed on server: " + response);
			String formatted = String.join("___n___", response.split("\n"));
			outToClient.println(formatted);
			inFromClient.close();
			outToClient.close();
			System.out.println("Request processed");
		} catch (IOException e) {
			//report exception somewhere.
			e.printStackTrace();
		}
	}
}

class UdpWorker implements Runnable {
	int portID;
	int clientID;
	BookServer bk;

	public UdpWorker(int portID, int clientID, BookServer bk) {
		this.portID = portID;
		this.clientID = clientID;
		this.bk = bk;
	}

	// create new datagram listener for client
	public void run() {
		try {
			DatagramPacket datapacket, returnpacket;
			DatagramSocket datasocket = new DatagramSocket(this.portID);
			while (true) {
				byte[] receive_buf = new byte[1024];
				datapacket = new DatagramPacket(receive_buf, receive_buf.length);
				datasocket.receive(datapacket);

				// receive command from client
				String command = new String(receive_buf, 0, datapacket.getLength());
				System.out.println("Received UDP Datapacket: " + command);

				String response = this.bk.handleRequest(command);
				byte[] send_buf = response.getBytes();

				returnpacket = new DatagramPacket(send_buf, send_buf.length,
						datapacket.getAddress(), datapacket.getPort());
				datasocket.send(returnpacket);
			}
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}




