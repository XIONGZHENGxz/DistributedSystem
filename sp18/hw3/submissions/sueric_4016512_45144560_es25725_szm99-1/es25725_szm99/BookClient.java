import java.util.Scanner;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.util.*;

public class BookClient {
	private static final int HANDSHAKE_BUFFER = 5;
	private static final int MAX_INPUT_BUFFER_SIZE = 2048;
	private static final int MAX_RETURN_BUFFER_SIZE = 4096;

	// Output string
	private static String bigString = "";

	// UDP uses Datagram, TCP uses Sockets
	private static DatagramSocket dgSocket;

	// Internet address is just localhost, UDP/TCP ports will be set to server
	// assigned port
	private static InetAddress ia;
	private static int udpPort;
	private static int tcpPort;

	// Client ID is determined from command line arg
	private static int clientId;

	// TCP/UDP flag, the default is UDP
	private static boolean isUDP;

	public static void main(String[] args) {
		String hostAddress;

		if (args.length != 2) {
			System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
			System.out.println("\t(1) <command-file>: file with commands to the server");
			System.out.println("\t(2) client id: an integer between 1..9");
			System.exit(-1);
		}

		isUDP = true;
		String commandFile = args[0];
		clientId = Integer.parseInt(args[1]);
		hostAddress = "localhost";
		tcpPort = -1; // -1 is the default, Socket needs to be configured
		udpPort = 8000;

		// Init the initial socket connection
		byte hBuff[] = new byte[HANDSHAKE_BUFFER];
		byte rBuff[] = new byte[MAX_RETURN_BUFFER_SIZE];

		try {
			ia = InetAddress.getByName(hostAddress);

			// Perform initial handshake to get UDP port
			dgSocket = new DatagramSocket();
			DatagramPacket handshake = new DatagramPacket(hBuff, hBuff.length, ia, udpPort);
			dgSocket.send(handshake);
			DatagramPacket rPacket = new DatagramPacket(rBuff, rBuff.length);
			dgSocket.receive(rPacket);

			// newPort is port number of worksocket
			String newPort = new String(rPacket.getData(), 0, rPacket.getLength());
			// Set the new UDP port that the client will use to work w/ server
			udpPort = Integer.parseInt(newPort);

			Scanner sc = new Scanner(new FileReader(commandFile));

			while (sc.hasNextLine()) {
				String cmd = sc.nextLine();
				String[] tokens = cmd.split(" ");

				if (tokens[0].equals("setmode")) {
					String result;
					if (isUDP == true) {
						result = queryServerUDP(cmd);
					} else {
						result = queryServerTCP(cmd);
					}
					String transLetter = result.split(" ")[0];
					int port = Integer.parseInt(result.split(" ")[1]);

					if (transLetter.equals("T")) {
						isUDP = false;
						tcpPort = port;
					} else {
						isUDP = true;
						udpPort = port;
					}
				} else if (tokens[0].equals("borrow")) {
					talkWithServer(cmd);
				} else if (tokens[0].equals("return")) {
					talkWithServer(cmd);
				} else if (tokens[0].equals("inventory")) {
					talkWithServer(cmd);
				} else if (tokens[0].equals("list")) {
					talkWithServer(cmd);
				} else if (tokens[0].equals("exit")) {
					if (isUDP == true)
						queryServerUDP(cmd);
					else
						queryServerTCP(cmd);

					writeToText(bigString, false);

					dgSocket.close();
					sc.close();
					System.exit(0);

				} else {
					System.out.println("ERROR: No such command");
				}
			}
			sc.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Determine whether TCP/UDP should be used for transmission. Append response to
	 * the final output.
	 * 
	 * Returns: String Server response
	 */
	private static void talkWithServer(String cmd) throws IOException {
		String res;
		if (isUDP == true) {
			res = queryServerUDP(cmd);
		} else {
			res = queryServerTCP(cmd);
		}
		res += "\n";
		bigString += res;
	}

	/**
	 * Query the server with a specific command and get the resulting response from
	 * the server. This is the TCP approach.
	 * 
	 * Returns: String containing the entirety of the server response.
	 */
	private static String queryServerTCP(String cmd) throws IOException {
		String resultStr = "";
		try (Socket tcpSocket = new Socket(ia, tcpPort);
				PrintStream streamToServer = new PrintStream(tcpSocket.getOutputStream(), true);
				BufferedReader readFromServer = new BufferedReader(
						new InputStreamReader(tcpSocket.getInputStream()));) {
			// Send the command
			streamToServer.println(cmd);

			// Read the data until we are done
//			String response
//			resultStr = readFromServer.readLine();
//			while (resultStr != null) {
//				resultStr += readFromServer.readLine();
//			}
			String response = "";
			while ((response = readFromServer.readLine()) != null) {
				resultStr += (response + "\n");
			}
		}

		return resultStr.trim();
	}

	/**
	 * Query the server with a specific command and get the resulting response from
	 * the server. This is the UDP approach.
	 * 
	 * Returns: String containing the entirety of the server response.
	 */
	private static String queryServerUDP(String cmd) throws IOException {
		// Send the entire command as a byte buffer
		String result;
		byte commandBuffer[] = new byte[cmd.length()];
		commandBuffer = cmd.getBytes();
		byte resultBuffer[] = new byte[MAX_RETURN_BUFFER_SIZE];
		DatagramPacket commandPacket = new DatagramPacket(commandBuffer, commandBuffer.length, ia, udpPort);
		DatagramPacket resultPacket = new DatagramPacket(resultBuffer, resultBuffer.length);

		dgSocket.send(commandPacket);
		dgSocket.receive(resultPacket);

		result = new String(resultPacket.getData(), 0, resultPacket.getLength());

		return result;

	}

	/**
	 * Client write to text file.
	 * 
	 * @param append
	 *            Specify whether or not to append or start at the top of the file
	 *            (destructive)
	 */
	private static void writeToText(String toText, boolean append) throws IOException {
		// File will be created if doesn't exist, open & append
		BufferedWriter out = new BufferedWriter(new FileWriter("out_" + BookClient.clientId + ".txt", append));
		out.write(toText);
		out.close();
	}
}
