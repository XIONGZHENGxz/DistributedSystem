import java.util.Scanner;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
public class BookClient {
	public static void main (String[] args) {
		if (args.length != 2) {
			System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
			System.out.println("\t(1) <command-file>: file with commands to the server");
			System.out.println("\t(2) client id: an integer between 1..9");
			System.exit(-1);
		}

		/* declaring variables */
		DatagramPacket sPacket, rPacket;
		byte[] rBuffer = new byte[1024];
		byte[] buffer = null;
		String hostAddress = "localhost";
		int tcpPort = 7000; // hardcoded -- must match the server's tcp port
		int udpPort = 8000; // hardcoded -- must match the server's udp port

		String commandFile = args[0];
		int clientId = Integer.parseInt(args[1]);
		String outputFile = "out_" + clientId + ".txt";

		String command = "";
		String[] tokens = null;
		String returnString = "";
		String protocol = "U";

		try {
			InetAddress IA = InetAddress.getByName(hostAddress);
			DatagramSocket dataSocket = new DatagramSocket();

			Scanner sc = new Scanner(new FileReader(commandFile));
			PrintWriter writer = new PrintWriter(outputFile, "UTF-8");
			PrintWriter close = new PrintWriter("inventory.txt", "UTF-8");

			
			
			Socket chatSocket = new Socket("localhost", tcpPort);
			PrintWriter send = new PrintWriter(chatSocket.getOutputStream());
			InputStreamReader stream = new InputStreamReader(chatSocket.getInputStream());
			BufferedReader test = new BufferedReader(stream);
			
			

			while(sc.hasNextLine()) {
				command = sc.nextLine();
				tokens = command.split(" ");

				if(protocol.equals("U")) {
					buffer = new byte[command.length()];
					buffer = command.getBytes();
					sPacket = new DatagramPacket(buffer, buffer.length, IA, udpPort);
					dataSocket.send(sPacket);
					rPacket = new DatagramPacket(rBuffer, rBuffer.length);
					dataSocket.receive(rPacket);
					returnString = new String(rPacket.getData(), 0, rPacket.getLength());

					if (tokens[0].equals("setmode")) {
						// TODO: set the mode of communication for sending commands to the server
						if(tokens[1].equals("T")) protocol = "T";
					}
					else if (tokens[0].equals("borrow")) {
						writer.println(returnString);
					} else if (tokens[0].equals("return")) {
						writer.println(returnString);
					} else if (tokens[0].equals("inventory")) {
						returnString = returnString.replace("^", "\n");
						writer.print(returnString); // not println!!!
					} else if (tokens[0].equals("list")) {
						returnString = returnString.replace("^", "\n");
						writer.print(returnString); // not println!!!
					} else if (tokens[0].equals("exit")) {
						returnString = returnString.replace("^", "\n");
						close.print(returnString); // not println!!!
						dataSocket.close();
						//sc.close();
						writer.close();
						close.close();
					} else {
						System.out.println("ERROR: No such command");
						System.out.println("Server: " + returnString);
					}
				}
				else {
					send.write(command + "\n");
					send.flush();
					String message = test.readLine();

					if (tokens[0].equals("setmode")) {
						// TODO: set the mode of communication for sending commands to the server
						if(tokens[1].equals("T")) {
							protocol = "T";
						}
						else {
							protocol = "U";
						}
					}
					else if (tokens[0].equals("borrow")) {
						writer.println(message);
					} else if (tokens[0].equals("return")) {
						writer.println(message);
					} else if (tokens[0].equals("inventory")) {
						message = message.replace("^", "\n");
						writer.print(message);
					} else if (tokens[0].equals("list")) {
						message = message.replace("^", "\n");
						writer.print(message);
					} else if (tokens[0].equals("exit")) {
						message = message.replace("^", "\n");
						close.print(message);
						//send.println("exit"); // sends exit command to server
						chatSocket.close();
						writer.close();
						close.close();

					} else {
						System.out.println("ERROR: No such command");
						System.out.println("Server: " + message);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}