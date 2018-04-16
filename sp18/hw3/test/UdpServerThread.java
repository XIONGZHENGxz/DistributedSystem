import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Scanner;

public class UdpServerThread extends Thread {
	// Const
	private static final String MALFORMED_MESSAGE = "Error: Malformed message.";
	private static final int PACKET_LENGTH = 1024;

	private BookServer bookServer;
	private DatagramSocket udpSocket;
	private byte[] receiveBuffer;
	private DatagramPacket receivedPacket;

	public UdpServerThread(BookServer bookServer) {
		this.bookServer = bookServer;
		udpSocket = bookServer.getUdpSocket();
		receiveBuffer = new byte[PACKET_LENGTH];
		receivedPacket = new DatagramPacket(receiveBuffer, PACKET_LENGTH);
	}

	@Override
	public void run() {
		try {
			while (true) {
				udpSocket.receive(receivedPacket);
				sendResponse();
				System.out.println("UDP set up");
			}
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	private void sendResponse() {
		String cmd = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
		String[] tokens = commandToArray(cmd);
		String message;
		System.out.println("message from client recieved by server: " + cmd);

		switch (tokens[0]) {
//          Expected: ['setmode', <'U'/'T'>] --> handled by clients
//          case "setmode":

			// Expected: ['borrow', <student name>, <book name>]
			case "borrow":
				if (tokens.length != 3) {
					System.out.println(MALFORMED_MESSAGE);
					break;
				}
				message = bookServer.borrow(tokens[1], tokens[2]) + " *message_end*";
				sendPacket(message);
				break;

			// Expected: ['return', <record id>]
			case "return":
				if (tokens.length != 2) {
					System.out.println(MALFORMED_MESSAGE);
					break;
				}
				message = bookServer.returnBook(Integer.valueOf(tokens[1])) + " *message_end*";
				sendPacket(message);
				break;

			// Expected: ['list', <student name>]
			case "list":
				if (tokens.length != 2) {
					System.out.println(MALFORMED_MESSAGE);
					break;
				}
				message = bookServer.list(tokens[1]) + " *message_end*";
				sendPacket(message);
				break;

			// Expected: ['inventory']
			case "inventory":
				if (tokens.length != 1) {
					System.out.println(MALFORMED_MESSAGE);
					break;
				}
				message = bookServer.inventory() + " *message_end*";
				sendPacket(message);
				break;

			// Expected: ['exit']
			case "exit":
				if (tokens.length != 1) {
					System.out.println(MALFORMED_MESSAGE);
					break;
				}
				message = bookServer.exit() + " *message_end*";
				sendPacket(message);
				return;

			default:
				break;
		}
	}

	// Tokenizes received commands to array form. If the message has
	// a quote, the whole quoted entry is input.
	//
	// Precondition: This does not take in strings less than length
	// 			two because they cannot be valid commands.
	public static String[] commandToArray(String cmd) {
		Scanner scanner = new Scanner(cmd);
		ArrayList<String> result = new ArrayList<>();
		boolean inQuote = false;
		String current;
		String arrayEntry = "";

		while (scanner.hasNext()) {
			current = scanner.next();
			char firstChar = current.charAt(0);
			char lastChar = current.charAt(current.length()-1);
			if (lastChar == '\"') {
				if (firstChar != '\"') {
					arrayEntry += (" ");
				}
				arrayEntry += current;
				result.add(arrayEntry);
				arrayEntry = "";
				inQuote = false;
			} else if (firstChar == '\"') {
				inQuote = true;
				arrayEntry += current;
			} else if(inQuote) {
				arrayEntry += (" " + current);
			} else {
				result.add(current);
			}
		}

		if (result.size() == 0) {
			return (new String[]{""});
		} else {
			return result.toArray(new String[result.size()]);
		}
	}

	// Sends a packet to "receivedPacket's" address with 'message'
	private void sendPacket(String message) {
		try {
			byte[] sendBuffer = message.getBytes();
			System.out.println("sending: " + message + " to client");
			DatagramPacket sendPacket = new DatagramPacket(sendBuffer,
					sendBuffer.length,
					receivedPacket.getAddress(),
					receivedPacket.getPort());
			bookServer.getUdpSocket().send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
