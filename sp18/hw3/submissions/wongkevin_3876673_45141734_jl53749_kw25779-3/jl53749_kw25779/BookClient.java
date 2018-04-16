//package homework3;

import java.util.Scanner;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

public class BookClient {
	public static void main(String[] args) {
		String hostAddress;
		int tcpPort;
		int udpPort;
		int clientId;

		boolean udp = true; // true if its udp false if tcp

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

		byte[] rbuffer = new byte[1024]; // might need to change later => copied
											// book
		DatagramPacket sPacket, rPacket;

		try {
			// Scanner sc = new Scanner(new FileReader(commandFile));
			
			/*
			StringBuilder input = new StringBuilder();
			input.append("/Users/justinliang/Documents/workspace/Concurrent/src/homework3/");
			input.append(args[0]);
			File file = new File(input.toString());
			Scanner sc = new Scanner(file);
			*/
			Scanner sc = new Scanner(new FileReader(args[0]));

			InetAddress ia = InetAddress.getByName(hostAddress);
			DatagramSocket datasocket = new DatagramSocket();
			
			/*
			StringBuilder output_file = new StringBuilder();
			output_file.append("/Users/justinliang/Documents/workspace/Concurrent/src/homework3/out_");
			output_file.append(args[1]);
			output_file.append(".txt");
			//System.out.println(output_file.toString());
			PrintStream out = new PrintStream(new FileOutputStream(output_file.toString()));
			System.setOut(out);
			*/
			
			StringBuilder output_file = new StringBuilder();
			output_file.append("out_");
			output_file.append(args[1]);
			output_file.append(".txt");
			PrintStream inv_out = new PrintStream(output_file.toString());
			System.setOut(inv_out);
			
			Socket tcpSocket = null; 
			
			
			
			BufferedReader in = null;
			DataOutputStream output = null;
			PrintStream sendToServer = null;

			while (sc.hasNextLine()) {
				String cmd = sc.nextLine();
				String[] tokens = cmd.split(" ");

				// byte[] buffer = new byte[echoline.length()];

				if (tokens[0].equals("setmode")) {
					// TODO: set the mode of communication for sending commands
					// to the server
					byte[] buffer = new byte[cmd.length()];
					buffer = cmd.getBytes();
					if (udp) {
						if (tokens[1].equals("T")) { //need to change modes

							udp = false;
							tcpSocket = new Socket (hostAddress, tcpPort);
							in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
							//output = new DataOutputStream (tcpSocket.getOutputStream());
							sendToServer = new PrintStream(tcpSocket.getOutputStream());
						}
						
						
					}
					else { //tcp
						if (tokens[1].equals("U")) {
							udp = true;
							tcpSocket.close();
						}
					}
				} else if (tokens[0].equals("borrow")) {
					//person = tokens[1];
					// System.out.println("HI1");
					byte[] buffer = new byte[cmd.length()];
					buffer = cmd.getBytes();
					if (udp) {

						sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
						datasocket.send(sPacket);
						rPacket = new DatagramPacket(rbuffer, rbuffer.length);
						// System.out.println(tokens[0]); //think it gets stuck
						// here
						datasocket.receive(rPacket);
						String retstring = new String(rPacket.getData(), 0, rPacket.getLength());
						System.out.println(retstring);
						// System.out.println("HI");
					} else {
						
						//output.writeBytes(cmd);
						sendToServer.println(cmd);
						String s = in.readLine();
						System.out.println(s);
						
						/*
						Scanner receiveFromServer = new Scanner(tcpSocket.getInputStream());
						System.out.println("YOYOYOYOYO");
						StringBuilder sb = new StringBuilder();
						String line;
						while (receiveFromServer.hasNext()) {
							sb.append(receiveFromServer.nextLine());
							sb.append("\n");
							System.out.print("HO");
						}
						System.out.println("HI");
						//System.out.println(sb.toString());
						receiveFromServer.close();
						*/
						
					}

					// TODO: send appropriate command to the server and display
					// the
					// appropriate responses form the server
				} else if (tokens[0].equals("return")) {
					
					byte[] buffer = new byte[cmd.length()];
					buffer = cmd.getBytes();
					if (udp) {

						sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
						datasocket.send(sPacket);
						rPacket = new DatagramPacket(rbuffer, rbuffer.length);

						datasocket.receive(rPacket);
						String retstring = new String(rPacket.getData(), 0, rPacket.getLength());
						System.out.println(retstring);

					}else {
						
						sendToServer.println(cmd);
						String s = in.readLine();
						System.out.println(s);
					}
					// TODO: send appropriate command to the server and display
					// the
					// appropriate responses form the server
				} else if (tokens[0].equals("inventory")) {
					
					byte[] buffer = new byte[cmd.length()];
					buffer = cmd.getBytes();
					if (udp) {

						sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
						datasocket.send(sPacket);
						rPacket = new DatagramPacket(rbuffer, rbuffer.length);

						datasocket.receive(rPacket);
						String retstring = new String(rPacket.getData(), 0, rPacket.getLength());
						System.out.print(retstring);

					}else {
						sendToServer.println(cmd);
						
						//System.out.println("THIS IS OUR INVENTORY STRING: "); 
						
						String curr_line = null;
						while(!(curr_line = in.readLine()).equals("")) {
							System.out.println(curr_line);
						}
						//System.out.println("Got out of INVENTORY STRING");
						
						
						//String s = in.readLine();
						//System.out.println("THIS IS OUR INVENTORY STRING: "+s);
					}
					// TODO: send appropriate command to the server and display
					// the
					// appropriate responses form the server
				} else if (tokens[0].equals("list")) {

					byte[] buffer = new byte[cmd.length()];
					buffer = cmd.getBytes();
					if (udp) {

						sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
						datasocket.send(sPacket);
						rPacket = new DatagramPacket(rbuffer, rbuffer.length);
						datasocket.receive(rPacket);
						String retstring = new String(rPacket.getData(), 0, rPacket.getLength());					
						System.out.print(retstring);
					}else {
						sendToServer.println(cmd);
						String curr_line = null;
						while(!(curr_line = in.readLine()).equals("")) {
							System.out.println(curr_line);
						}
					}
					
					// TODO: send appropriate command to the server and display
					// the
					// appropriate responses form the server
				} else if (tokens[0].equals("exit")) {
					//System.out.println(output_file.toString());
					/*
					PrintStream inv_out = new PrintStream("/Users/justinliang/Documents/workspace/Concurrent/src/homework3/inventory.txt");
					System.setOut(inv_out);
					*/
					inv_out = new PrintStream("inventory.txt");
					System.setOut(inv_out);
					byte[] buffer = new byte[cmd.length()];
					buffer = cmd.getBytes();
					sc.close();
					
					
					if (udp) {

						sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
						datasocket.send(sPacket);
						rPacket = new DatagramPacket(rbuffer, rbuffer.length);

						datasocket.receive(rPacket);
						String retstring = new String(rPacket.getData(), 0, rPacket.getLength());
						System.out.print(retstring);

					}else {
						
						sendToServer.println(cmd);
						String curr_line = null;
						while(!(curr_line = in.readLine()).equals("")) {
							System.out.println(curr_line);
						}
						tcpSocket.close();
					}
					datasocket.close();
					break;
					// TODO: send appropriate command to the server
				} else {
					System.out.println("ERROR: No such command");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			System.err.println(e);
		} catch (SocketException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}

	}
	public static void sendPacket(String cmd) {
		
	}
}
