import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookClient {
	private final static Logger LOGGER = Logger.getLogger(BookClient.class.getName());
	private final static int TCP_PORT = 7000;
	private final static int UDP_PORT = 8000;
	private final static String HOSTNAME = "localhost";

	private static boolean tcp = false;

	static FileWriter out;
	
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
			System.out.println("\t(1) <command-file>: file with commands to the server");
			System.out.println("\t(2) client id: an integer between 1..9");
			System.exit(-1);
		}
		
		try {
			out = new FileWriter(new File("out_"+args[1]+".txt"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
//		try {
//			System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("out_"+args[1]+".txt"))));
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

		try {
			Scanner sc = new Scanner(new File(args[0]));
			while (sc.hasNextLine()) {
				sendCommand(sc.nextLine());
			}
			sc.close();
			out.close();
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "File not found: " + args[0]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static boolean sendCommand(String command) {
		if (command.startsWith("setmode")) {
			if (command.endsWith("T")) {
				tcp = true;
			} else {
				tcp = false;
			}
			return true;
		}

		command += '\n';
		if (tcp) {
			try (Socket tcpSocket = new Socket(HOSTNAME, TCP_PORT)) {

				Scanner server = new Scanner(tcpSocket.getInputStream());
				tcpSocket.getOutputStream().write(command.getBytes());
				int lines = server.nextInt();
				server.nextLine();
				for (int x = 0; x < lines; x++) {
					String s = server.nextLine();
					System.out.println(s);
					out.write(s + "\n");
				}
				server.close();
			} catch (UnknownHostException e2) {
				LOGGER.log(Level.SEVERE, "Cannot connect to " + HOSTNAME);
				return false;
			} catch (IOException e1) {
				LOGGER.log(Level.SEVERE, "Cannot connect to " + HOSTNAME);
			}
		} else {
			try (DatagramSocket udpSocket = new DatagramSocket()) {
				byte[] buffer = command.getBytes();
				byte[] rbuffer = new byte[1024];
				udpSocket.send(new DatagramPacket(buffer, buffer.length, InetAddress.getByName(HOSTNAME), UDP_PORT));
				DatagramPacket receive = new DatagramPacket(rbuffer, rbuffer.length);
				udpSocket.receive(receive);
				int lines = Integer.parseInt(new String(receive.getData()).trim());
				for (int x = 0; x < lines; x++) {
					rbuffer = new byte[1024];
					receive = new DatagramPacket(rbuffer, rbuffer.length);
					udpSocket.receive(receive);
					
					String s = new String(receive.getData()).trim();
					System.out.println(s);
					out.write(s + "\n");
				}
				
			} catch (SocketException e3) {
				LOGGER.log(Level.SEVERE, "Cannot bind UDP socket");
				return false;
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Cannot connect to " + HOSTNAME);
				return false;
			}
		}
		return true;
	}
}