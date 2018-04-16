import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class BookServer {
	// Const
	private static final int TCP_PORT = 7000;
	private static final int UDP_PORT = 8000;

	private DatagramSocket udpSocket;	// UDP socket if protocol == UDP
	private ServerSocket tcpSocket;		// TCP server socket if protocol == TCP
	private int transactionId;			// Id of book checkout, begins at 1

	private Map<String, Integer> inventoryMap;				// Maps book name to quantity available
	private Map<String, Queue<Transaction>> borrowedMap;	// Maps students to their list of transactions (for 'list')
	private Map<Integer, Transaction> idMap;				// Maps ids to their transactions (for 'returnBook')
	private Queue<String> orderedInventory;					// Order of books input (for 'inventory')

	public static class Transaction {
		public int id;
		public String student;
		public String book;

		public Transaction(int id, String student, String book) {
			this.id = id;
			this.student = student;
			this.book = book;
		}
	}

	/** ---------- BookServer Methods ---------- **/

	public BookServer() throws IOException {
		udpSocket = new DatagramSocket(UDP_PORT);
		tcpSocket = new ServerSocket(TCP_PORT);
		transactionId = 1;
		inventoryMap = new HashMap<>();
		borrowedMap = new HashMap<>();
		idMap = new HashMap<>();
		orderedInventory = new LinkedList<>();
	}

	// Adds books and their quantities from 'filename' to 'inventoryMap'
	public void populateInventory(String filename) throws FileNotFoundException {
		File inputFile = new File(filename);
		Scanner fileScanner = new Scanner(inputFile);

		while (fileScanner.hasNextLine()) {
			String entry = fileScanner.nextLine();
			int iLastQuote = entry.lastIndexOf("\"");

			String name = entry.substring(0, iLastQuote + 1);
			int quantity = Integer.valueOf(entry.substring(iLastQuote + 2, entry.length()));
			inventoryMap.put(name, quantity);
			orderedInventory.add(name);
		}
	}

	private void listenTcp() throws IOException {
		// TCP loop
		while (true) {
			Socket client = tcpSocket.accept(); //puts on different port if possible, else IOException
			System.out.println("established tcp connection with client");
			Thread t = new TcpServerThread(this, client);
			t.start();
		}
	}

	//	public synchronized String setmode() { --> handled by client
	//		return "We changed the mode.";
	//	}
	// Borrows 'book' to 'student' and updates maps
	public synchronized String borrow(String student, String book) {
		Integer numLeft = inventoryMap.get(book);
		if (numLeft == null) {
			return "Request Failed - We do not have this book";
		} else if (numLeft == 0) {
			return "Request Failed - Book not available";
		}
		inventoryMap.replace(book, numLeft - 1);
		updateBorrowedMap(student, book, transactionId);
		Transaction t = new Transaction(transactionId, student, book);
		idMap.put(transactionId, t);

		return "Your request has been approved, " + (transactionId++) + " " + student + " " + book;
	}

	// Updates student's borrowed map or creates a new one if needed
	// TODO: does list mean list whole history, or just currently borrowed?
	private synchronized void updateBorrowedMap(String student, String book, int id) {
		Queue<Transaction> studentQueue = borrowedMap.get(student);
		if (studentQueue == null) {
			borrowedMap.put(student, new LinkedList<>());
			studentQueue = borrowedMap.get(student);
		}
		Transaction t = new Transaction(id, student, book);
		studentQueue.add(t);
	}

	public synchronized String returnBook(int id) {
		Transaction t = idMap.get(id);
		if (t == null) {
			return (id + " not found, no such borrow record");
		}
		// Increase in inventoryMap
		int quantity = inventoryMap.get(t.book);
		inventoryMap.replace(t.book, quantity + 1);

		// Remove from student's borrowed list
		Queue<Transaction> q = borrowedMap.get(t.student);
		for (Transaction entry : q) {
			if (entry.id == id) {
				q.remove(entry);
			}
		}
		idMap.remove(id);

		return (id + " is returned");
	}

	// TODO: does list mean list whole history, or just currently borrowed?
	// Lists the student's currently borrowed list
	public synchronized String list(String student) {
		Queue<Transaction> q = borrowedMap.get(student);
		if (q == null || q.size() == 0) {
			return ("No record found for " + student);
		}

		StringBuilder result = new StringBuilder();
		for (Transaction t : q) {
			result.append(t.id);
			result.append(" ");
			result.append(t.book);
			result.append("\n");
		}
		result.delete(result.length()-1, result.length());	// Remove extra newline
		return result.toString();
	}

	public synchronized String inventory() {
		StringBuilder result = new StringBuilder();
		for (String book : orderedInventory) {
			int quantity = inventoryMap.get(book);
			result.append(book);
			result.append(" ");
			result.append(quantity);
			result.append("\n");
		}
		if (result.length() > 0) {
			result.delete(result.length()-1, result.length());	// Remove extra newline
		}
		return result.toString();
	}

	//client closes sockets
	public synchronized String exit() {
		try {
			writeInventory();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "exited successfully";
	}

	// Used to close ports for unit testing
	public void closeAll() {
		try {
			tcpSocket.close();
			udpSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private synchronized void writeInventory() throws IOException {
		String[] wordArray = {this.inventory()};
		List<String> words = Arrays.asList(wordArray);
		Path file = Paths.get("inventory.txt");

		if (this.inventory().equals("")) {
			PrintWriter writer = new PrintWriter("inventory.txt");
			writer.close();
		} else {
			Files.write(file, words);
		}
	}

	public DatagramSocket getUdpSocket() {
		return udpSocket;
	}

	/** ---------- Main ---------- **/

	public static void main(String[] args) {
		try {
			BookServer bookServer = new BookServer();
			if (args.length != 1) {
				System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
				System.exit(-1);
			}
			bookServer.populateInventory(args[0]);

			Thread udpListener = new UdpServerThread(bookServer);
			udpListener.start();
			bookServer.listenTcp();
			udpListener.join(); //todo: why do we need this?
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
