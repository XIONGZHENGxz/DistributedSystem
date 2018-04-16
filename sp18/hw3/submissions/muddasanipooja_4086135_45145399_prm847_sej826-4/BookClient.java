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
		boolean isUDP = true;

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

		int len = 1024;
		byte[] rbuffer = new byte[len];
		DatagramSocket datasocket = null;
		DatagramPacket sPacket, rPacket;
		Socket sock = null;
		InputStreamReader serverStream;
		PrintWriter tcpOut;
		BufferedReader tcpIn;
		Scanner sc = null;
		PrintWriter fileOut;
		try {
			InetAddress ia = InetAddress.getByName(hostAddress);
			datasocket = new DatagramSocket();
			sock = new Socket(hostAddress, tcpPort);
			sc = new Scanner(new FileReader(commandFile));
			fileOut=new PrintWriter(new FileWriter("out_"+clientId+".txt")) ;

			while (sc.hasNextLine()) {
				String cmd = sc.nextLine();
				String[] tokens = cmd.split(" ");
				String msg = "";
				rPacket = null;
				if (!tokens[0].equals("setmode")) {
					if (isUDP) {
						byte[] buffer = new byte[cmd.length()];
						buffer = cmd.getBytes();
						sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
						datasocket.send(sPacket);
						rPacket = new DatagramPacket(rbuffer, rbuffer.length);
						datasocket.receive(rPacket);
						msg = new String(rPacket.getData(), 0, rPacket.getLength());
					}
					else{
						tcpOut = new PrintWriter(sock.getOutputStream(), true);
						serverStream = new InputStreamReader(sock.getInputStream());
						tcpIn = new BufferedReader(serverStream);
						tcpOut.println(cmd);
						if(!tokens[0].equals("exit")){
							msg = tcpIn.readLine();
						}
					}
				}

				if (tokens[0].equals("setmode")) {
					if (tokens[1].equals("T")) {
						isUDP = false;
					}
					if (tokens[1].equals("U")) {
						isUDP = true;
					}
				} else if (tokens[0].equals("borrow")) {
					fileOut.println(msg);
				} else if (tokens[0].equals("return")) {
					fileOut.println(msg);
				} else if (tokens[0].equals("inventory")) {
					String[] words = msg.split("%");
					for(String word: words){
						fileOut.println(word);						
					}
				} else if (tokens[0].equals("list")) {
					String[] words = msg.split("%");
					for(String word: words){
						fileOut.println(word);						
					}
				} else if (tokens[0].equals("exit")) {
					fileOut.close();
					sock.close();
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
		} finally {
			if (sc != null) {
				sc.close();
			}
			if (datasocket != null) {
				datasocket.close();
			}
		}
	}
}
