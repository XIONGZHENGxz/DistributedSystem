/*
 * UTEIDs: csf596, cfd363
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;

public class BookClient {
	
	private static boolean usingTCP;
	private static String hostAddress;
	private static int tcpPort;
	private static int udpPort;
	private static DatagramSocket udpSocket;
	private static Socket tcpSocket;
	private static DataInputStream tcpIn;
	private static DataOutputStream tcpOut;
	

	public static void main(String[] args) {
		int clientId = 1;
		usingTCP = false;

		if (args.length != 2) {
			System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
			System.out.println("\t(1) <command-file>: file with commands to the server");
			System.out.println("\t(2) client id: an integer between 1..9");
			System.exit(-1);
		}

		String commandFile = args[0];
		clientId = Integer.parseInt(args[1]);
		
		//String commandFile = "cmdFile.txt";
		
		hostAddress = "localhost";
		tcpPort = 7000;// hardcoded -- must match the server's tcp port
		udpPort = 8000;// hardcoded -- must match the server's udp port
		
		try { //Initialize Sockets
			udpSocket = new DatagramSocket();
			tcpSocket = new Socket(InetAddress.getByName(hostAddress), tcpPort);
			tcpIn = new DataInputStream(tcpSocket.getInputStream());
			tcpOut = new DataOutputStream(tcpSocket.getOutputStream());
			
            BufferedWriter writer = new BufferedWriter(new FileWriter("out_"+clientId+".txt"));
    		
			Scanner sc = new Scanner(new FileReader(commandFile));

			while (sc.hasNextLine()) {
				String cmd = sc.nextLine();
				String[] tokens = cmd.split(" ");
				String response = null;
				if (tokens[0].equals("setmode")) {
					// TODO: set the mode of communication for sending commands to the server
					if (tokens[1].equals("U")) {
						usingTCP = false;
					} else if (tokens[1].equals("T")) {
						usingTCP = true;
					}
				} else if (tokens[0].equals("borrow")) {
					// TODO: send appropriate command to the server and display the
					// appropriate responses form the server
					sendPacket(cmd);
					response = receiveMessage();

				} else if (tokens[0].equals("return")) {
					// TODO: send appropriate command to the server and display the
					// appropriate responses form the server
					sendPacket(cmd);
					response = receiveMessage();

				} else if (tokens[0].equals("inventory")) {
					// TODO: send appropriate command to the server and display the
					// appropriate responses form the server
					sendPacket(cmd);
					response = receiveMessage();

				} else if (tokens[0].equals("list")) {
					// TODO: send appropriate command to the server and display the
					// appropriate responses form the server
					sendPacket(cmd);
					response = receiveMessage();
				} else if (tokens[0].equals("exit")) {
					// TODO: send appropriate command to the server
					sendPacket(cmd);
					tcpSocket.close();
					udpSocket.close();
					writer.close();
					System.exit(0);
				} else {
					System.out.println("ERROR: No such command");
				}
				if (response != null) {
					String[] responseTokens = response.split("\n");
					for (String s : responseTokens) {
						writer.write(s);
						writer.newLine();
					}
				}
			}
			writer.close();
			tcpSocket.close();
			udpSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void sendPacket(String cmd) {
		try {
			if (!usingTCP) { //UDP
				System.out.println("Sending UDP: "+cmd);
				byte[] buf = cmd.getBytes();
				DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(hostAddress), udpPort);
				udpSocket.send(packet);
				
			} else { //TCP
				System.out.println("Sending TCP: "+cmd);
				tcpOut.writeUTF(cmd);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String receiveMessage() {
		try {
			if (!usingTCP) { //UDP
				byte[] buffer = new byte[256];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				udpSocket.receive(packet); //block until you receive packet
				return new String(packet.getData(), 0, packet.getLength());
			} else { //TCP
				return tcpIn.readUTF();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}