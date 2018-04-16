import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;

public class BookClient {

	LinkedList<String> result = new LinkedList<>();

	Scanner din;
	PrintStream pout;
	Socket server;

	DatagramPacket datapacket, returnpacket;
	DatagramSocket datasocket;
	String[] finalInv;
	byte[] buf;
	int packetPort;
	InetAddress ia;

	String hostAddress = "localhost";
	int tcpPort = 7000;// hardcoded -- must match the server's tcp port
	int udpPort = 8000;// hardcoded -- must match the server's udp port

	public void getSocket(String addr, int port) throws IOException {
		//System.out.println(addr + " " + port);
		server = new Socket(addr, port);
		din = new Scanner(server.getInputStream());
		pout = new PrintStream(server.getOutputStream());
	}

	public void uinitPackets(int port) throws IOException {
		datasocket = new DatagramSocket();
		buf = new byte[1000];
		packetPort = port;
		//System.out.println(packetPort);
		ia = InetAddress.getByName(hostAddress);
	}

	public void borrow(String student, String name) throws IOException {
		String s;
		pout.println("borrow " + student + " " + name);
		pout.flush();
		int retValue = din.nextInt();
		if (retValue == 0) {
			s = "Request Failed - Book not available";
			System.out.println(s + " TCP");
		} else if (retValue == -1) {
			s = "Request Failed - We do not have this book";
			System.out.println(s + " TCP");
		} else {
			s = "Your request has been approved, " + retValue + " " + student + " " + name;
			System.out.println(s + " TCP");
		}
		result.add(s);
	}

	private void uborrow(String student, String name) {
		// TODO Auto-generated method stub
		String s;
		String message = "borrow " + student + " " + name;
		byte[] buf_data = new byte[1000];
		buf_data = message.getBytes();
		datapacket = new DatagramPacket(buf_data, buf_data.length, ia, packetPort);
		try {
			datasocket.send(datapacket);
			//System.out.println(new String(datapacket.getData()));
			buf = new byte[1000];
			returnpacket = new DatagramPacket(buf, buf.length);
			datasocket.receive(returnpacket);
			//System.out.println(new String(returnpacket.getData()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int retValue = Integer.parseInt(new String(returnpacket.getData()).split("\0")[0]);
		if (retValue == 0) {
			s = "Request Failed - Book not available";
			System.out.println(s);
		} else if (retValue == -1) {
			s = "Request Failed - We do not have this book";
			System.out.println(s);
		} else {
			s = "Your request has been approved, " + retValue + " " + student + " " + name;
			System.out.println(s);
		}
		result.add(s);
	}

	public void returnBook(int id) throws IOException {
		String s;
		pout.println("return " + id);
		pout.flush();
		int retValue = din.nextInt();
		if (retValue == 0) {
			s = id + " not found, no such borrow record";
		} else {
			s = id + " is returned";
		}
		System.out.println(s + " TCP");
		result.add(s);
	}

	private void ureturnBook(int id) {
		// TODO Auto-generated method stub
		String s;
		String message = "return " + id;
		byte[] buf_data = new byte[1000];
		buf_data = message.getBytes();
		datapacket = new DatagramPacket(buf_data, buf_data.length, ia, packetPort);
		try {
			datasocket.send(datapacket);
			buf = new byte[1000];
			returnpacket = new DatagramPacket(buf, buf.length);
			datasocket.receive(returnpacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String response = new String(returnpacket.getData(), 0, returnpacket.getLength());
		int retValue = Integer.parseInt(response.split("\0")[0]);
		if (retValue == 0) {
			s = id + " not found, no such borrow record";
		} else {
			s = id + " is returned";
		}
		System.out.println(s);
		result.add(s);
	}

	public void List(String student) throws IOException {
		pout.println("list " + student);
		pout.flush();
		String s = null;
		String r = "";
		while ((s = din.nextLine()) != null) {
			if (s.length() != 0) {
				r += s;
				break;
			}
		}
		String[] tokens = r.split("\\*");
		for (String line : tokens) {
			if (!line.isEmpty()) {
				System.out.println(line);
				result.add(line);
			}
		}
	}

	private void uList(String student) {
		// TODO Auto-generated method stub
		String s;
		String message = "list " + student;
		byte[] buf_data = new byte[1000];
		buf_data = message.getBytes();
		datapacket = new DatagramPacket(buf_data, buf_data.length, ia, packetPort);
		try {
			datasocket.send(datapacket);
			buf = new byte[1000];
			returnpacket = new DatagramPacket(buf, buf.length);
			datasocket.receive(returnpacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String response = new String(returnpacket.getData(), 0, returnpacket.getLength());
		s = new String(response.split("\0")[0]);
		String[] tokens = s.split("\\*");
		for (String line : tokens) {
			if (line.isEmpty())
				;
			else {
				System.out.println(line);
				result.add(line);
			}
		}
	}

	public void inventory() throws IOException {
		pout.println("inventory");
		pout.flush();
		String s = null;
		String r = "";
		while ((s = din.nextLine()) != null) {
			if (s.length() != 0) {
				r += s;
				break;
			}
		}
		String[] tokens = r.split("\\*");
		for (String line : tokens) {
			if (!line.isEmpty()) {
				System.out.println(line);
				result.add(line);
			}
		}
	}

	private void uinventory() {
		// TODO Auto-generated method stub
		String s;
		String message = "inventory";
		byte[] buf_data = new byte[1000];
		buf_data = message.getBytes();
		datapacket = new DatagramPacket(buf_data, buf_data.length, ia, packetPort);
		try {
			datasocket.send(datapacket);
			buf = new byte[1000];
			returnpacket = new DatagramPacket(buf, buf.length);
			datasocket.receive(returnpacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String response = new String(returnpacket.getData(), 0, returnpacket.getLength());
		s = new String(response.split("\0")[0]);
		String[] tokens = s.split("\\*");
		for (String line : tokens) {
			if (!line.isEmpty()) {
				System.out.println(line);
				result.add(line);
			}
		}
	}

	public void exit() throws IOException {
		pout.println("exit");
		String s = null;
		String r = "";
		while ((s = din.nextLine()) != null) {
			if (s.length() != 0) {
				r += s;
				break;
			}
		}
		pout.flush();
		server.close();
		finalInv = r.split("\\*");
	}

	private void uexit() {
		// TODO Auto-generated method stub
		String message = "exit";
		byte[] buf_data = new byte[1000];
		buf_data = message.getBytes();
		datapacket = new DatagramPacket(buf_data, buf_data.length, ia, packetPort);
		try {
			datasocket.send(datapacket);
			buf = new byte[1000];
			returnpacket = new DatagramPacket(buf, buf.length);
			datasocket.receive(returnpacket);
		} catch (IOException e) {
			e.printStackTrace();
		}

		finalInv = new String(returnpacket.getData()).split("\0")[0].split("\\*");
		datasocket.close();
	}

	public static void main(String[] args) {
		BookClient myClient = new BookClient();

		boolean Tmode = false;
		boolean set = false;
		int clientId;

		if (args.length != 2) {
			System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
			System.out.println("\t(1) <command-file>: file with commands to the server");
			System.out.println("\t(2) client id: an integer between 1..9");
			System.exit(-1);
		}

		String commandFile = args[0];
		clientId = Integer.parseInt(args[1]);

		try {
			Scanner sc = new Scanner(new FileReader(commandFile));

			myClient.getSocket(myClient.hostAddress, myClient.udpPort);
			int port = myClient.din.nextInt();
			myClient.server.close();
			myClient.uinitPackets(port);

			//contacting the listener
			while (sc.hasNextLine()) {
				String cmd = sc.nextLine();
				String[] tokens = cmd.split(" ");

				if (tokens[0].equals("setmode")) {
					// TODO: set the mode of communication for sending commands to the server
					if (tokens[1].equals("T")) {
						Tmode = true;
						if (!set) {
							myClient.getSocket(myClient.hostAddress, myClient.tcpPort);
							int tcpport = myClient.din.nextInt();
							myClient.server.close();
							myClient.getSocket(myClient.hostAddress, tcpport);
							set = true;
						}
					} else if (tokens[1].equals("U")) {
						Tmode = false;
					}
				} else if (tokens[0].equals("borrow")) {
					String student = tokens[1];
					String bookName = tokens[2];
					if (tokens.length > 3) {
						int i = 3;
						while (i < tokens.length) {
							bookName = bookName + " " + tokens[i];
							i++;
						}
					}
					if (Tmode) {
						myClient.borrow(student, bookName);
					} else {
						myClient.uborrow(student, bookName);
					}
					// TODO: send appropriate command to the server and display the
					// appropriate responses form the server
				} else if (tokens[0].equals("return")) {
					int id = Integer.parseInt(tokens[1]);
					if (Tmode) {
						myClient.returnBook(id);
					} else {
						myClient.ureturnBook(id);
					}
					// TODO: send appropriate command to the server and display the
					// appropriate responses form the server
				} else if (tokens[0].equals("inventory")) {
					if (Tmode) {
						myClient.inventory();
					} else {
						myClient.uinventory();
					}
					// TODO: send appropriate command to the server and display the
					// appropriate responses form the server
				} else if (tokens[0].equals("list")) {
					String student = tokens[1];
					if (Tmode) {
						myClient.List(student);
					} else {
						myClient.uList(student);
					}
					// TODO: send appropriate command to the server and display the
					// appropriate responses form the server
				} else if (tokens[0].equals("exit")) {
					if (Tmode) {
						System.out.println("before break");
						myClient.exit();
						myClient.pout.close();
						System.out.println("after break");
						break;

					} else {
						System.out.println("before break");
						myClient.uexit();
						System.out.println("after break");
						break;
					}
					// TODO: send appropriate command to the server
				} else {
					System.out.println("ERROR: No such command");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			//System.out.println("writing out");
			String fileName = "out_" + clientId + ".txt";
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			for (String s : myClient.result) {
				s = s + "\n";
				writer.write(s);
			}
			writer.close();
			//System.out.println("writing inv");
			String fileInventory = "inventory.txt";
			writer = new BufferedWriter(new FileWriter(fileInventory));
			for (String s : myClient.finalInv) {
				s = s + "\n";
				writer.write(s);
			}
			writer.close();
			System.out.println("done " + clientId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}