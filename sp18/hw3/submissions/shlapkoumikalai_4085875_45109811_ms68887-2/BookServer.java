import java.util.*;
import java.net.*;
import java.io.*;

public class BookServer {
	private static final int SERVER_PORT = 2018;
	private static final int INIT_TCP_PORT = 7000;
	private static final int INIT_UDP_PORT = 8000;
	
	private static int tcpPort;
	private static int udpPort;

	private static List<InventoryItem> inventory;
	private static List<Record> records;
	private static int recordCount;

	private static class InventoryItem {
		private String book;
		private int count;

		InventoryItem(String book, int count) {
			this.book = book;
			this.count = count;
		}
	}

	private static class Record {
		private int recordID;
		private String student;
		private String book;

		Record(String student, String book, int recordID) {
			this.recordID = recordID;
			this.student = student;
			this.book = book;
		}
	}

	private static synchronized String borrowBook(String student, String book) {
		InventoryItem targetItem = null;
		for (InventoryItem item : inventory) {
			if (item.book.equals(book)) {
				targetItem = item;
				break;
			}
		}

		if (targetItem == null) {
			return "Request Failed - We do not have this book";
		} else if (targetItem.count == 0) {
			return "Request Failed - Book not available";
		}

		targetItem.count--;
		records.add(new Record(student, book, recordCount));

		return "Your request has been approved, " + recordCount++ + " " + student + " " + book;
	}

	private static synchronized String returnBook(int recordID) {
		Record targetRecord = null;
		for (Record record : records) {
			if (record.recordID == recordID) {
				targetRecord = record;
				break;
			}
		}

		if (!(targetRecord == null)) {
			for (InventoryItem item : inventory) {
				if (item.book.equals(targetRecord.book)) {
					item.count++;
					break;
				}
			}
			records.remove(targetRecord);
			return recordID + " is returned";
		} else {
			return recordID + " not found, no such borrow record";
		}
	}

	private static synchronized String list(String student) {
		StringBuilder bookList = new StringBuilder();

		for (Record record : records) {
			if (record.student.equals(student)) {
				bookList.append(record.recordID).append(" ").append(record.book).append(",");
			}
		}

		if (bookList.length() == 0) {
			return "No record found for " + student;
		} else {
			return bookList.toString();
		}
	}

	private static synchronized String inventory() {
		StringBuilder inventoryList = new StringBuilder();

		for (InventoryItem item : inventory) {
			inventoryList.append(item.book).append(" ").append(item.count).append(",");
		}

		return inventoryList.toString();
	}

	private static synchronized void exit() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("inventory.txt"));
			for (int i = 0; i < inventory.size(); i++) {
				writer.write(inventory.get(i).book + " " + inventory.get(i).count + "\n");
				//writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		inventory =  Collections.synchronizedList(new ArrayList<>());
		records =  Collections.synchronizedList(new ArrayList<>());
		recordCount = 1;

		if (args.length != 1) {
			System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
			System.exit(-1);
		}

		String fileName = args[0];
		tcpPort = INIT_TCP_PORT;
		udpPort = INIT_UDP_PORT;

		try {
			Scanner sc = new Scanner(new FileReader(fileName));

			while (sc.hasNext()) {
				StringBuilder bookName = new StringBuilder();
				while (!sc.hasNextInt()) {
					bookName.append(sc.next());
					if (!sc.hasNextInt())
						bookName.append(" ");
				}
				inventory.add(new InventoryItem(bookName.toString(), sc.nextInt()));
			}

			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		DatagramPacket datapacket, returnpacket;
		int len = 1024;
		try {
			DatagramSocket datasocket = new DatagramSocket(SERVER_PORT);
			byte[] buf = new byte[len];
			while (true) {
				datapacket = new DatagramPacket(buf, buf.length);
				datasocket.receive(datapacket);
				String receiveString = new String(datapacket.getData(), 0, datapacket.getLength());
				String returnPort = getNewPort(receiveString);
				buf = returnPort.getBytes();
				returnpacket = new DatagramPacket(buf, buf.length, datapacket.getAddress(), datapacket.getPort());
				datasocket.send(returnpacket);
			}
		} catch (SocketException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}
	}
	
	private static synchronized String getNewPort(String receiveString) throws IOException
	{
		String returnPort = "";
		if (receiveString.equals("T")) {
			// make a TCP port from 7000-7999
			returnPort = String.valueOf(tcpPort);
			ServerSocket socket = new ServerSocket(tcpPort);
			TCPServerReceiver tcpReceiverThread = new TCPServerReceiver(socket);
			tcpReceiverThread.start();
			tcpPort++;
			if (tcpPort == 7999)
				tcpPort = INIT_TCP_PORT;
		} else if (receiveString.equals("U")) {
			// make a UDP port from 8000-8999
			returnPort = String.valueOf(udpPort);
			DatagramSocket socket = new DatagramSocket(udpPort);
			UDPServerReceiver udpReceiverThread = new UDPServerReceiver(socket);
			udpReceiverThread.start();
			udpPort++;
			if (udpPort == 8999)
				udpPort = INIT_UDP_PORT;
		}
		return returnPort;
	}

	private static class UDPServerReceiver extends Thread {
		DatagramSocket datasocket;

		UDPServerReceiver(DatagramSocket s) {
			datasocket = s;
		}

		public void run() {
			DatagramPacket datapacket, returnpacket;
			int len = 1024;
			try {
				byte[] buf = new byte[len];
				while (true) {
					datapacket = new DatagramPacket(buf, buf.length);
					datasocket.receive(datapacket);
					String receiveString = new String(datapacket.getData(), 0, datapacket.getLength());
					Scanner st = new Scanner(receiveString);
					String tag = st.next();

					switch (tag)
					{
						case "T":
						{
							//System.out.println("UDP attempting setmode");
							//need to swap from UDP to TCP, else we just stay on UDP
							String returnPort = getNewPort("T");
							buf = returnPort.getBytes();
							returnpacket = new DatagramPacket(buf, buf.length, datapacket.getAddress(), datapacket.getPort());
							datasocket.send(returnpacket);
							datasocket.close();
							st.close();
							return;
						}
						case "borrow":
						{
							//System.out.println("borrow");
							String student = st.next();
							StringBuilder bookName = new StringBuilder();
							while (st.hasNext())
							{
								bookName.append(st.next());
								if (st.hasNext())
									bookName.append(" ");
							}
							String output = borrowBook(student, bookName.toString());

							buf = output.getBytes();
							returnpacket = new DatagramPacket(buf, buf.length, datapacket.getAddress(), datapacket.getPort());
							datasocket.send(returnpacket);
							break;
						}
						case "return":
						{
							int recordID = st.nextInt();
							String output = returnBook(recordID);

							buf = output.getBytes();
							returnpacket = new DatagramPacket(buf, buf.length, datapacket.getAddress(), datapacket.getPort());
							datasocket.send(returnpacket);
							break;
						}
						case "inventory":
							String inventoryList = inventory();

							buf = inventoryList.getBytes();
							returnpacket = new DatagramPacket(buf, buf.length, datapacket.getAddress(), datapacket.getPort());
							datasocket.send(returnpacket);
							break;
						case "list":
						{
							String student = st.next();
							String bookList = list(student);

							buf = bookList.getBytes();
							returnpacket = new DatagramPacket(buf, buf.length, datapacket.getAddress(), datapacket.getPort());
							datasocket.send(returnpacket);
							break;
						}
						case "exit":
							exit();
							datasocket.close();
							st.close();
							return;
					}
				}
			} catch (SocketException e) {
				System.err.println(e);
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}

	private static class TCPServerReceiver extends Thread {
		ServerSocket serverSocket;

		TCPServerReceiver(ServerSocket s) {
			serverSocket = s;
		}

		public void run() {
			Socket s;
			try {
				while ((s = serverSocket.accept()) != null) {
					try {
						Scanner sc = new Scanner(s.getInputStream());
						PrintWriter pout = new PrintWriter(s.getOutputStream());
						String command = sc.nextLine();
						Scanner st = new Scanner(command);
						String tag = st.next();

						switch (tag)
						{
							case "U":
							{
								//System.out.println("TCP attempting setmode");
								String returnPort = getNewPort("U");
								pout.println(returnPort);
								pout.flush();
								s.close();
								sc.close();
								st.close();
								serverSocket.close();
								return;
							}
							case "borrow":
							{
								String student = st.next();
								StringBuilder bookName = new StringBuilder();
								while (st.hasNext())
								{
									bookName.append(st.next());
									if (st.hasNext())
										bookName.append(" ");
								}
								String output = borrowBook(student, bookName.toString());
								pout.println(output);
								break;
							}
							case "return":
							{
								int recordID = st.nextInt();
								String output = returnBook(recordID);
								pout.println(output);
								break;
							}
							case "inventory":
								String inventoryList = inventory();
								pout.println(inventoryList);
								break;
							case "list":
							{
								String student = st.next();
								String bookList = list(student);
								pout.println(bookList);
								break;
							}
							case "exit":
								exit();
								s.close();
								sc.close();
								st.close();
								serverSocket.close();
								return;
						}
						pout.flush();
					} catch (IOException e) {
						System.err.println(e);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
