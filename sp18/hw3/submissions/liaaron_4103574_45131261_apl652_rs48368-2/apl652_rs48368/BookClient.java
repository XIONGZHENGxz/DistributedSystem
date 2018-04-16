import java.net.*;
import java.util.Scanner;
import java.io.*;


public class BookClient {
	private static final String HOST_ADDRESS = "localhost";
	private static PrintWriter clientFile = null;

	//TCP variables
	private static final int TCP_PORT = 7000;
	private static Socket server = null;

	//UDP variables
	private static final int UDP_PORT = 8000;
	private static DatagramSocket initialSocket = null;

	public enum Protocol {
		UDP, TCP
	}

	private static void send_receive_UDP(String cmd, InetAddress hostIa) throws IOException {
		// Send
		byte[] sendBuffer = cmd.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, hostIa, UDP_PORT);
		initialSocket.send(sendPacket);

		// Receive
		byte[] receiveBuffer = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
		initialSocket.receive(receivePacket);
		String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
		if(!receivedMessage.equals("exited successfully *message_end*")){
			clientFile.println(receivedMessage.substring(0, receivedMessage.indexOf(" *message_end*")));
		}
	}

	private static void send_receive_TCP(String cmd) throws IOException {
		// Send
		PrintStream pout = new PrintStream(server.getOutputStream());
		pout.println(cmd);
		pout.flush();

		// Retrieve
		Scanner sc = new Scanner(server.getInputStream());
		String receivedMessage = sc.nextLine();
		if(!receivedMessage.equals("exited successfully *message_end*")) { //output nothing on exit request
			while(!receivedMessage.contains("*message_end*")){ //handle multi-line receivedMessage
				clientFile.println(receivedMessage);
				receivedMessage = sc.nextLine();
			}
			clientFile.println(receivedMessage.substring(0, receivedMessage.indexOf(" *message_end*")));
		}
	}

	public static void main(String[] args) throws SocketException, FileNotFoundException {
		Protocol protocol = Protocol.UDP; //default is UDP
		initialSocket = new DatagramSocket();

		if (args.length != 2) {
			System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
			System.out.println("\t(1) <command-file>: file with commands to the server");
			System.out.println("\t(2) client id: an integer between 1..9");
			System.exit(-1);
		}

		String commandFile = args[0];
		int clientId = Integer.parseInt(args[1]); //todo: should we through exception if 2 clients have same id?
		String file = "out_" + clientId + ".txt";
		clientFile = new PrintWriter(file);

		try {
			InetAddress hostIa = InetAddress.getByName(HOST_ADDRESS);
			Scanner sc = new Scanner(new FileReader(commandFile));

			while (sc.hasNextLine()) {
				String cmd = sc.nextLine();
				String[] tokens = cmd.split(" ");

				System.out.println("Sent packet: " + cmd);

				if (tokens[0].equals("setmode")) { //hardcoded - no server request required
					if (tokens[1].equals("T")) {
						protocol = Protocol.TCP;
						if(server == null) { server = new Socket(hostIa, TCP_PORT); }
					}else if(tokens[1].equals("U")){ //incase client wants to use TCP then decides to use UDP
						protocol = Protocol.UDP; //default is UDP
						//UDP socket has already been setup
					}
				} else {
					if (protocol == Protocol.UDP && !tokens[0].equals("setmode")) {
						send_receive_UDP(cmd, hostIa);
					} else if (protocol == Protocol.TCP && !tokens[0].equals("setmode")) {
						send_receive_TCP(cmd);
					}

					if (tokens[0].equals("exit")) { //client closes sockets, server only prints
						initialSocket.close();
						if(server != null){	server.close(); }
						clientFile.close();
						System.out.println("*****client exiting"); //debugging
					}
				}
			}
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (SocketException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
