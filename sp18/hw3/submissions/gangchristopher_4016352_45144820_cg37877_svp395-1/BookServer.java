import java.util.*;
import java.io.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;

public class BookServer {
	// contains list of books and quantity from inventory.txt
	static BookInventory bookInventory = new BookInventory();

	// contains list of people and books checked out
	static ArrayList<PersonInventory> people = new ArrayList<PersonInventory>();

	// ID given to identify a specific book checkout
	static int recordID = 1;

	/**
	* @param record ID of book checkout
	* @return if record ID exists, index of person with record ID, else -1
	*/
	public static int listContainsRecordID(int recordID) {
		synchronized(people){
			for (int i = 0; i < people.size(); i++) {
				if (people.get(i).containsRecordID(recordID))
					return i;
			}
		}
		return -1;
	}

	/**
	* @param name of person
	* @return return true if person exists in list
	*/
	public static boolean personListed(String name) {
		synchronized(people) {
			for (int i = 0; i < people.size(); i++) {
				if (people.get(i).getName().equals(name))
					return true;
			}
		}
		return false;
	}

	/**
	* @param name of person
	* @return if person exists, return PersonInventory of specified person, else null
	*/
	public static PersonInventory getPerson(String name) {
		synchronized(people) {
			for (int i = 0; i < people.size(); i++) {
				if (people.get(i).getName().equals(name))
					return people.get(i);
			}
		}
		return null;
	}

	/**
	* Creates new thread to handle one client with either 
	* TCP or UDP connection.
	*
	* @param client address, client port, client ID
	*/
	public static void newClientThread(InetAddress ia, int port, int clientID) {
		Thread t = new Thread () {
			DatagramSocket udpSocket = null;	// UDP socket for UDP communication
			ServerSocket tcpSocket = null;		// TCP socket for TCP communication
			Socket clientSocket = null;			// client socket for TCP comm.
			PrintWriter tcpOut = null;			// allows server to write to client
			BufferedReader tcpIn = null;		// allows client to write to server

			DatagramPacket sPacket, rPacket;	// DatagramPackets for sending and receiving via UDP connection
			String[] rData;						// will contain parsed command from client
			String send;						// will contain data to be sent to client
			String receive;						// will contain unparsed command from client
			boolean UDP = true;					// indicates mode of communication with client
			boolean initializedTCP = false;		// indicates if TCP connection has been initialized
			byte[] sbuffer = new byte[1024];	// buffer that will contain data to be sent to client
			byte[] rbuffer = new byte[1024];	// buffer that will contain data from client

			public void run() {
				try {
					// initialize UDP socket
					udpSocket = new DatagramSocket();

					// initialize TCP socket
					tcpSocket = new ServerSocket(0);

					// send port number of new thread server to client
					send = Integer.toString(udpSocket.getLocalPort()) + " 123";
					sbuffer = send.getBytes();
					sPacket = new DatagramPacket(sbuffer, sbuffer.length, ia, port);
					udpSocket.send(sPacket);

					// initialize file writer
					PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("out_" + clientID + ".txt", true)));

					while (true) {
						// UDP connection
						while (UDP) {
							// receive command from client
							rPacket = new DatagramPacket(rbuffer, rbuffer.length);
							udpSocket.receive(rPacket);
							rData = new String(rbuffer).split(" ");
							receive = new String(rbuffer);
							rbuffer = new byte[1024];
							sbuffer = new byte[1024];

							switch (rData[0].trim()) {
							case "setmode":
								// set mode to UDP comm.
								if (rData[1].trim().equals("U")) {
									UDP = true;
								}
								// set mode to TCP comm.
								// initialize TCP comm.
								else if (rData[1].trim().equals("T") && !initializedTCP) {
									UDP = false;
									initializedTCP = true;

									// Send TCP port number to client using UDP connection
									send = Integer.toString(tcpSocket.getLocalPort()) + " 123";
									sbuffer = send.getBytes();
									sPacket = new DatagramPacket(sbuffer, sbuffer.length, ia, port);
									udpSocket.send(sPacket);

									// Accept connection from TCP client
									clientSocket = tcpSocket.accept();

									// Establish means of communication with client
									tcpOut = new PrintWriter(clientSocket.getOutputStream(), true);
									tcpIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
								}
								// set mode to TCP comm.
								else if (rData[1].trim().equals("T")) {
									UDP = false;
								}
								break;
							case "borrow":
								String studentName = rData[1];
								String bookName = receive;
								bookName = bookName.substring(bookName.indexOf("\"") + 1);
								bookName = bookName.substring(0, bookName.indexOf("\""));

								// if person already exists in records, borrow book from bookInventory
								// also add book to person's inventory record
								if (personListed(studentName)) {
									synchronized(bookInventory) {
										if (getPerson(studentName).borrowBook(bookInventory, bookName, recordID, "out_" + clientID + ".txt")) {
											recordID++;
										}
									}
								}
								// else initialize person in records and borrow book from bookInventory
								// also add book to person's inventory record
								else {
									PersonInventory person = new PersonInventory(studentName);
									synchronized(bookInventory) {
										if (person.borrowBook(bookInventory, bookName, recordID, "out_" + clientID + ".txt")) {
											recordID++;
										}
									}
									synchronized(people){
										people.add(person);
									}
								}
								break;
							case "return":
								int id = Integer.parseInt(rData[1].trim());
								// if the record ID exists then return the book
								if (listContainsRecordID(id) != -1) {
									people.get(listContainsRecordID(id)).returnBook(bookInventory, "out_" + clientID + ".txt", id);
								} 
								// if the record ID doesn't exist print "not found"
								else if (listContainsRecordID(id) == -1) {
									out.println(id + " not found");
								}
								break;
							case "inventory":
								// output a list of the current book inventory
								File file = new File("inventory.txt");
								file.delete();
								synchronized (bookInventory) {
									bookInventory.getInventory("out_" + clientID + ".txt");
								}
								break;
							case "list":
								// list the specified person's inventory record
								String name = rData[1];
								if (personListed(name.trim())) {
									getPerson(name.trim()).listBooks("out_" + clientID + ".txt");
								} else {
									out.println("No record found for " + name);
								}
								break;
							case "exit":
								// output a list of the current book inventory
								synchronized (bookInventory) {
									bookInventory.getInventory("inventory.txt");
								}
								// close the file writer
								out.close();
								// stop the thread
								return;
							default:
								break;
							}
						}


						// TCP connection
						while (!UDP) {
							String input;
							while ((input = tcpIn.readLine()) != null) {
								rData = input.split(" ");
								receive = input;
								switch (rData[0].trim()) {
								case "setmode":
									// set mode to UDP comm.
									if (rData[1].trim().equals("U")) {
										UDP = true;
									} 
									// set mode to TCP comm.
									else if (rData[1].trim().equals("T")) {
										UDP = false;
									}
									break;
								case "borrow":
									String studentName = rData[1];
									String bookName = receive;
									bookName = bookName.substring(bookName.indexOf("\"") + 1);
									bookName = bookName.substring(0, bookName.indexOf("\""));

									// if person already exists in records, borrow book from bookInventory
									// also add book to person's inventory record
									if (personListed(studentName)) {
										synchronized(bookInventory) {
											if (getPerson(studentName).borrowBook(bookInventory, bookName, recordID, "out_" + clientID + ".txt")) {
												recordID++;
											}
										}
									} 
									// else initialize person in records and borrow book from bookInventory
									// also add book to person's inventory record
									else {
										PersonInventory person = new PersonInventory(studentName);
										synchronized(bookInventory) {
											if (person.borrowBook(bookInventory, bookName, recordID, "out_" + clientID + ".txt")) {
												recordID++;
											}
										}
										synchronized(people){
											people.add(person);
										}
									}
									break;
								case "return":
									int id = Integer.parseInt(rData[1].trim());
									// if the record ID exists then return the book
									if (listContainsRecordID(id) != -1) {
										synchronized(people){
											people.get(listContainsRecordID(id)).returnBook(bookInventory, "out_" + clientID + ".txt", id);
										}
									} 
									// if the record ID doesn't exist print "not found"
									else if (listContainsRecordID(id) == -1) {
										out.println(id + " not found");
									}
									break;
								case "inventory":
									// output a list of the current book inventory
									synchronized (bookInventory) {
										File file = new File("inventory.txt");
										file.delete();
										bookInventory.getInventory("out_" + clientID + ".txt");
									}
									break;
								case "list":
									String name = rData[1];
									// list the specified person's inventory record
									if (personListed(name.trim())) {
										getPerson(name.trim()).listBooks("out_" + clientID + ".txt");
									} else {
										out.println("No record found for " + name);
									}
									break;
								case "exit":
									// output a list of the current book inventory
									synchronized (bookInventory) {
										bookInventory.getInventory("inventory.txt");
									}
									// close the file writer
									out.close();
									// stop the thread
									return;
								default:
									break;
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		t.start();
	}

	public static void main (String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
			System.exit(-1);
		}

		DatagramSocket mainSocket;
		DatagramPacket rPacket;
		byte[] rbuffer;
		String[] rData;
		String receive;
		String fileName = args[0];
		int tcpPort = 7000;
		int udpPort = 8000;


		// Delete all previous output files
		for (int i = 0; i < 10; i++) {
			File file = new File("out_" + i + ".txt");
			file.delete();
		}

		// Initialize stock of inventory
		Scanner inv = new Scanner(new FileReader(fileName));
		while (inv.hasNextLine()) {
			String line = inv.nextLine();
			String bookName = line;
			bookName = bookName.substring(bookName.indexOf("\"") + 1);
			bookName = bookName.substring(0, bookName.indexOf("\""));
			int bookQuantity = Integer.parseInt(line.replaceAll("[\\D]", ""));
			synchronized (bookInventory) {
				bookInventory.addInventory(bookName, bookQuantity);
			}
		}

		try {
			mainSocket = new DatagramSocket(udpPort);

			// This "thread" is responsible for receiving new client connections
			while (true) {
				rbuffer = new byte[1024];
				rPacket = new DatagramPacket(rbuffer, rbuffer.length);
				mainSocket.receive(rPacket);
				rData = new String(rbuffer).split(" ");
				newClientThread(rPacket.getAddress(), rPacket.getPort(), Integer.parseInt(rData[0]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}