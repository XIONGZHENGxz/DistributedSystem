import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.PriorityQueue;

public class BookServer {
	AtomicInteger currentId = new AtomicInteger(1);

	public static void main(String[] args) {
		int tcpPort;
		int udpPort;
		HashMap<String, Integer> Books = new HashMap<>();
		HashMap<Integer, String[]> Record = new HashMap<>();
		ArrayList<String> BookList = new ArrayList<>();
		if (args.length != 1) {
			System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
			System.exit(-1);
		}

		String fileName = args[0];
		tcpPort = 7000;
		udpPort = 8000;

		// parse the inventory file
		FileReader fr = null;
		BufferedReader br = null;
		try {

			// br = new BufferedReader(new FileReader(FILENAME));
			fr = new FileReader(fileName);
			br = new BufferedReader(fr);

			String line;

			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("\"");
				Books.put(tokens[1], new Integer(tokens[2].substring(1)));
				BookList.add(tokens[1]);
			}

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}

		Thread t1 = new Thread(new BookServer().new UDPServer(Books, BookList, Record, udpPort));
		Thread t2 = new Thread(new BookServer().new TCPServer(Books, BookList, Record, tcpPort));
		t1.start();
		t2.start();

	}

	synchronized String borrow(String name, String book, HashMap<Integer, String[]> record,
			HashMap<String, Integer> inventory) {
		String msg;
		if (inventory.containsKey(book) && inventory.get(book).intValue() > 0) {
			// create an id
			int id = currentId.getAndIncrement();
			msg = "Your request has been approved " + id + " " + name + " " + book;
			String[] pair = { name, book };
			record.put(id, pair);
			inventory.replace(book, new Integer(inventory.get(book).intValue() - 1));
		} else {
			msg = "Request Failed - We do not have this book";
		}
		return msg;
	}

	synchronized String returned(Integer id, HashMap<Integer, String[]> record, HashMap<String, Integer> inventory) {
		String msg;
		if (record.keySet().contains(id)) {
			// find the book from record
			String[] pair = record.get(id);
			String book = pair[1];
			// update inventory to show one more of the book
			inventory.replace(book, new Integer(inventory.get(book).intValue() + 1));
			// delete record
			record.remove(id);
			msg = id + " is returned";
		} else {
			msg = id + " not found, no such borrow record";
		}
		return msg;

	}

	synchronized String list(String name, HashMap<Integer, String[]> record) {
		String msg = "";
		PriorityQueue<Integer> idList = new PriorityQueue<Integer>();
		idList.addAll(record.keySet());
		// go through record and find every instance with name
		// for each instance of name in record
		while (!idList.isEmpty()) {
			Integer id = idList.poll();
			String[] pair = record.get(id);
			if (pair[0].equals(name)) {
				msg += id.toString() + " \"" + pair[1] + "\"%";
			}
		}
		if (msg.length() == 0) {
			msg = "No record found for " + name;
		}
		return msg.substring(0, msg.length() - 1);
	}

	synchronized String inventory(HashMap<String, Integer> Books, ArrayList<String> BookList) {
		String msg = "";
		// for each item in inventory
		for (String book : BookList) {
			msg += "\"" + book + "\" " + Books.get(book).toString() + '%';
		}
		return msg.substring(0, msg.length() - 1);
	}

	class UDPServer implements Runnable {
		HashMap<String, Integer> Books;
		ArrayList<String> BookList;
		HashMap<Integer, String[]> Record;
		int port;

		UDPServer(HashMap<String, Integer> Books, ArrayList<String> BookList, HashMap<Integer, String[]> Record,
				int port) {
			this.Books = Books;
			this.port = port;
			this.BookList = BookList;
			this.Record = Record;
		}

		@Override
		public void run() {
			DatagramPacket datapacket, spacket;
			DatagramSocket datasocket = null;
			int len = 1024;
			try {
				datasocket = new DatagramSocket(port);
				byte[] buf = new byte[len];
				while (true) {
					datapacket = new DatagramPacket(buf, buf.length);
					datasocket.receive(datapacket);
					String cmd = new String(datapacket.getData(), 0, datapacket.getLength());
					String[] tokens = cmd.split(" ");
					if (tokens[0].equals("borrow")) {
						String[] new_toks = cmd.split("\"");
						String msg = borrow(tokens[1], new_toks[1], Record, Books);
						byte[] buffer = new byte[msg.length()];
						buffer = msg.getBytes();
						spacket = new DatagramPacket(buffer, buffer.length, datapacket.getAddress(),
								datapacket.getPort());
						datasocket.send(spacket);
					} else if (tokens[0].equals("return")) {
						String msg = returned(new Integer(tokens[1]), Record, Books);
						byte[] buffer = new byte[msg.length()];
						buffer = msg.getBytes();
						spacket = new DatagramPacket(buffer, buffer.length, datapacket.getAddress(),
								datapacket.getPort());
						datasocket.send(spacket);
					} else if (tokens[0].equals("inventory")) {
						String msg = inventory(Books, BookList);
						byte[] buffer = new byte[msg.length()];
						buffer = msg.getBytes();
						spacket = new DatagramPacket(buffer, buffer.length, datapacket.getAddress(),
								datapacket.getPort());
						datasocket.send(spacket);
					} else if (tokens[0].equals("list")) {
						String msg = list(tokens[1], Record);
						byte[] buffer = new byte[msg.length()];
						buffer = msg.getBytes();
						spacket = new DatagramPacket(buffer, buffer.length, datapacket.getAddress(),
								datapacket.getPort());
						datasocket.send(spacket);
					} else if (tokens[0].equals("exit")) {
						String msg = "Request  Failed  -  We  do  not  have  this  book";
						byte[] buffer = new byte[msg.length()];
						buffer = msg.getBytes();
						spacket = new DatagramPacket(buffer, buffer.length, datapacket.getAddress(),
								datapacket.getPort());
						datasocket.send(spacket);
					} else {
						String msg = "ERROR: No Such Command!";
						byte[] buffer = new byte[msg.length()];
						buffer = msg.getBytes();
						spacket = new DatagramPacket(buffer, buffer.length, datapacket.getAddress(),
								datapacket.getPort());
						datasocket.send(spacket);
					}
				}
			} catch (SocketException e) {
				System.err.println(e);
			} catch (IOException e) {
				System.err.println(e);
			} finally {
				datasocket.close();
			}
		}

	}

	class TCPServerFunctions implements Runnable {
		HashMap<String, Integer> Books;
		ArrayList<String> BookList;
		HashMap<Integer, String[]> Record;
		int port;
		Socket sock;

		TCPServerFunctions(HashMap<String, Integer> Books, ArrayList<String> BookList,
				HashMap<Integer, String[]> Record, int port, Socket sock) {
			this.Books = Books;
			this.port = port;
			this.BookList = BookList;
			this.Record = Record;
			this.sock = sock;
		}

		public void run() {
			BufferedReader reader;
			PrintWriter writer;
			String cmd;

			try {
				reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));

				writer = new PrintWriter(sock.getOutputStream(), true);
				while ((cmd = reader.readLine()) != null) {

					String[] tokens = cmd.split(" ");
					if (tokens[0].equals("borrow")) {
						String[] new_toks = cmd.split("\"");
						String msg = borrow(tokens[1], new_toks[1], Record, Books);
						writer.println(msg);

					} else if (tokens[0].equals("return")) {
						String msg = returned(new Integer(tokens[1]), Record, Books);
						writer.println(msg);

					} else if (tokens[0].equals("inventory")) {
						String msg = inventory(Books, BookList);
						writer.println(msg);

					} else if (tokens[0].equals("list")) {
						String msg = list(tokens[1], Record);
						writer.println(msg);

					} else if (tokens[0].equals("exit")) {
						break;
					} else {
						String msg = "ERROR: No Such Command!";
						writer.println(msg);
					}
				}
			} catch (NumberFormatException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	class TCPServer implements Runnable {
		HashMap<String, Integer> Books;
		ArrayList<String> BookList;
		HashMap<Integer, String[]> Record;
		int port;

		TCPServer(HashMap<String, Integer> Books, ArrayList<String> BookList, HashMap<Integer, String[]> Record,
				int port) {
			this.Books = Books;
			this.port = port;
			this.BookList = BookList;
			this.Record = Record;
		}

		@Override
		public void run() {
			ServerSocket servSocket = null;
			Socket sock = null;
			try {
				servSocket = new ServerSocket(port);
				while(true) {
					sock = servSocket.accept();
					Thread t = new Thread(new BookServer().new TCPServerFunctions(Books, BookList, Record, port, sock));
					t.start();
				}

			} catch (SocketException e) {
				System.err.println(e);
			} catch (IOException e) {
				System.err.println(e);
			} finally {

			}
		}
	}

}
