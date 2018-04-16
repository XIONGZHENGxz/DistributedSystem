/*
 * Gilad Croll - GC24654
 * Andoni Mendoza - AM46882
 */

import java.util.Scanner;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
public class BookClient {
	public static void main (String[] args) throws FileNotFoundException, UnsupportedEncodingException {
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

		int UDP = 0;
		int TCP = 1;      	
		int curPro = UDP; // set protocol to use
		String hostname = "localhost";
		DatagramPacket sPacket;
		int len = 1024;
		byte[] rbuffer = new byte[len];

		String fileName = "out_" + clientId + ".txt";
		PrintWriter writer = new PrintWriter(fileName, "UTF-8");

		try {
			Scanner sc = new Scanner(new FileReader(commandFile));
			InetAddress ia = InetAddress.getByName(hostname);
			DatagramSocket datasocket = new DatagramSocket();

			while(sc.hasNextLine()) {
				String cmd = sc.nextLine();
				String[] tokens = cmd.split(" ");

				if (tokens[0].equals("setmode")) {
					if (tokens[1].equals("U"))
						curPro = UDP;
					else{
						curPro = TCP;						
					}
				}
				else if (tokens[0].equals("borrow")) {
					// TODO: send appropriate command to the server and display the
					// appropriate responses form the server
					if (curPro == UDP){						
						handleUDP(rbuffer,writer, datasocket, cmd, udpPort, ia);
					}
					else{ // TCP - borrow
						handleTCP(hostname, tcpPort, cmd, writer);
					}

				} else if (tokens[0].equals("return")) {
					// TODO: send appropriate command to the server and display the
					// appropriate responses form the server
					if (curPro == UDP){
						handleUDP(rbuffer,writer, datasocket, cmd, udpPort, ia);
					}
					else{	// TCP - return
						handleTCP(hostname, tcpPort, cmd, writer);
					}
				} else if (tokens[0].equals("inventory")) {
					// TODO: send appropriate command to the server and display the
					// appropriate responses form the server
					if (curPro == UDP){
						handleUDP(rbuffer,writer, datasocket, cmd, udpPort, ia);
					}
					else{	// TCP - inventory
						handleTCP(hostname, tcpPort, cmd, writer);
					}
				} else if (tokens[0].equals("list")) {
					if (curPro == UDP){
						handleUDP(rbuffer,writer, datasocket, cmd, udpPort, ia);
					}
					else{	// TCP - list
						handleTCP(hostname, tcpPort, cmd, writer);
					}
				} else if (tokens[0].equals("exit")) {
					if (curPro == UDP){
						byte[] buffer = new byte[cmd.length()];
						buffer = cmd.getBytes();
						sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
						datasocket.send(sPacket);						
					}
					else{	// TCP - exit
						Socket server = new Socket(hostname, tcpPort);
						PrintStream pout = new PrintStream(server.getOutputStream());
						pout.println(cmd);
						pout.flush();
						server.close();
					}
					writer.close();
					sc.close();
					System.exit(1);
				} else {
					System.out.println("ERROR: No such command");
				}
			}
		} catch (Exception e) {e.printStackTrace();}
	}

	public static void handleTCP(String hostname, int tcpPort, String cmd, PrintWriter writer){
		try {
			Socket server = new Socket(hostname, tcpPort);
			Scanner din = new Scanner(server.getInputStream());
			PrintStream pout = new PrintStream(server.getOutputStream());
			pout.println(cmd);
			pout.flush();
			while (din.hasNextLine()){
				String retstring = din.nextLine();
				writer.println(retstring);	// write to file
			}
			server.close();
			din.close();
		} catch (Exception e) {}
	}

	public static void handleUDP(byte[] rbuffer,PrintWriter writer, DatagramSocket datasocket, String cmd, int udpPort, InetAddress ia){
		try{
			byte[] buffer = new byte[cmd.length()];
			buffer = cmd.getBytes();
			DatagramPacket sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
			datasocket.send(sPacket);

			DatagramPacket rPacket = new DatagramPacket(rbuffer, rbuffer.length);
			datasocket.receive(rPacket);
			String retstring = new String(rPacket.getData(), 0, rPacket.getLength());
			writer.println(retstring);
		}catch(Exception e){}
	}
}
