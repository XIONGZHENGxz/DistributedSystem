import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
public class BookClient {
	
	private static boolean mode;
	
	private static Socket Tsocket;
	private static InputStream reader;
	private static OutputStream writer;
	
	private static DatagramSocket socket;
	private static InetAddress address;
	private static int port;
	private static DatagramPacket inpacket;
	private static DatagramPacket outpacket;
	
	private static PrintWriter outputWriter;
	private static int len = 1024;
	private static byte[] inbuf = new byte[len];
	private static byte[] outbuf;
	private static ByteArrayOutputStream cmdStream;
	private static ObjectOutput outObj;
	
	private static void processCommand(String[] command) throws IOException {
		String message;
		cmdStream = new ByteArrayOutputStream();
		outObj = new ObjectOutputStream(cmdStream);
		outObj.writeObject(command);
		outObj.flush();
		outbuf = cmdStream.toByteArray();
		outObj.close();
		if (mode) {
			outpacket = new DatagramPacket(outbuf, outbuf.length, address, port);
			inpacket = new DatagramPacket(inbuf, inbuf.length);
			socket.send(outpacket);				
			socket.receive(inpacket);
			message = new String(inpacket.getData(),0,inpacket.getLength());
		}
		else {
			writer.write(outbuf, 0, outbuf.length);
			writer.flush();
			message = new String(inbuf,0,reader.read(inbuf, 0, inbuf.length));
		}
		if (command[0].equals("setmode")) {
			System.out.println(message); // debugging purposes
			return;
		}
		if (command[0].equals("exit")) {
			PrintWriter invWriter = new PrintWriter("inventory.txt");
			invWriter.println(message);
			invWriter.flush();
			invWriter.close();
			return;
		}
		outputWriter.println(message);
		outputWriter.flush();
	}
	
	public static void connectUDP(InetAddress ia, int udpPort) throws IOException {
		socket = new DatagramSocket();
		socket.setSoTimeout(1000);
		outbuf = "test".getBytes();
		boolean received = false;
		while (!received) {
			try {
				outpacket = new DatagramPacket(outbuf, outbuf.length, ia, udpPort);
				inpacket = new DatagramPacket(inbuf, inbuf.length);
				socket.send(outpacket);
				socket.receive(inpacket);
				String message = new String(inpacket.getData(),0,inpacket.getLength());
				if (!message.equals("routing")) {
					throw new IllegalArgumentException();
				}
				address = inpacket.getAddress();
				port = inpacket.getPort();
				received = true;
			} catch (SocketTimeoutException e) {
				System.out.println(e); // debugging purposes
			} catch (IllegalArgumentException e) {
				System.out.println(e); // debugging purposes
			}
		}
		socket.setSoTimeout(0);
	}
	
	public static void main (String[] args) {
		String hostAddress;
	    int tcpPort;
	    int udpPort;
	    int clientId;
	
	    if (args.length != 2) {
			System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
			System.out.println("\t(1) <command-file>: file with commands to the server");
			System.out.println("\t(2) client id: an integer between 1..9");
			System.exit(-1);
	    }
	
		String commandFile = args[0];
		clientId = Integer.parseInt(args[1]);
		hostAddress = "localhost";
		tcpPort = 7000;// hardcoded -- must match the server's tcp port
		udpPort = 8000;// hardcoded -- must match the server's udp port

		try {
			// establish connection
			mode = true;
			InetAddress ia  = InetAddress.getByName(hostAddress);
			connectUDP(ia,udpPort);
			
			// open output file to write to
			outputWriter = new PrintWriter("out_" + clientId + ".txt");
			
			// parse commands
			Scanner sc = new Scanner(new FileReader(commandFile));
			while(sc.hasNextLine()) {
				String cmd = sc.nextLine();
				String[] tokens = cmd.split(" ");				
				if (tokens[0].equals("setmode")) {
					boolean newmode = tokens[1].equals("U") ? true : false;
					if (newmode != mode) {
						String[] command = {tokens[0], tokens[1]};
						processCommand(command);
						if (tokens[1].equals("U")) {
							Tsocket.close();
							connectUDP(ia,udpPort);
						}
						else {
							socket.close();
							Tsocket = new Socket(hostAddress,tcpPort);
							reader = Tsocket.getInputStream();
							writer = Tsocket.getOutputStream();
						}
						mode = newmode;
					}
				} else if (tokens[0].equals("borrow")) {
					String[] command = {tokens[0], tokens[1], cmd.split("\"")[1]};
					processCommand(command);									
				} else if (tokens[0].equals("return")) {
					String[] command = {tokens[0], tokens[1]};
					processCommand(command);
				} else if (tokens[0].equals("inventory")) {
					String[] command = {tokens[0]};
					processCommand(command);
				} else if (tokens[0].equals("list")) {
					String[] command = {tokens[0], tokens[1]};
					processCommand(command);
				} else if (tokens[0].equals("exit")) {
					String[] command = {tokens[0]};
					processCommand(command);
				} else {
					System.out.println("ERROR: No such command");
				}
			}
			sc.close();
			outputWriter.close();
		    socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
