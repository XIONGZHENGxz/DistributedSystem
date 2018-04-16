import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class BookClient {
	Scanner din;
	PrintStream pout;
	Socket server;
	
	public static void main(String[] args) {
		String hostAddress;
		int tcpPort;
		int udpPort;
		int clientId;
		
		BookClient bookClient = new BookClient();
		
		boolean TCPFlag = false;
		
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
			Scanner sc = new Scanner(new FileReader(commandFile));

			InetAddress ia = InetAddress.getByName(hostAddress);
			DatagramSocket datasocket = new DatagramSocket();
					
			while (sc.hasNextLine()) {
				
				String cmd = sc.nextLine();
				String[] tokens = cmd.split(" ");

				if (tokens[0].equals("setmode")) {
					String mode = tokens[1];
			
					if (mode.equals("T") && TCPFlag == false)  {
						TCPFlag = true;
					}
					else if (mode.equals("U") && TCPFlag == true) {
						TCPFlag = false;
					}
					
				} else if (tokens[0].equals("borrow")) {
					
					if (TCPFlag) {
						String s = bookClient.sendAndRecieveTCP(hostAddress, cmd, tcpPort);
						write(s, clientId);
					}
					else {
						String s = bookClient.sendAndReceiveUDP(ia, datasocket, cmd, udpPort);
						write(s, clientId);
					}
				} else if (tokens[0].equals("return")) {
					if (TCPFlag) {
						String s = bookClient.sendAndRecieveTCP(hostAddress, cmd, tcpPort);
						write(s, clientId);
					}
					else {
						String s = bookClient.sendAndReceiveUDP(ia, datasocket, cmd, udpPort);
						write(s, clientId);	
					}
				} else if (tokens[0].equals("inventory")) {
					if (TCPFlag) {
						String s = bookClient.sendAndRecieveTCP(hostAddress, cmd, tcpPort);
						write(s, clientId);
					}
					else {
						String s = bookClient.sendAndReceiveUDP(ia, datasocket, cmd, udpPort);
						write(s, clientId);
					}
				} else if (tokens[0].equals("list")) {
					if (TCPFlag) {
						String s = bookClient.sendAndRecieveTCP(hostAddress, cmd, tcpPort);
						write(s, clientId);
					}
					else {
						String s = bookClient.sendAndReceiveUDP(ia, datasocket, cmd, udpPort);
						write(s, clientId);	
					}
				} else if (tokens[0].equals("exit")) {
					String s = new String("");
					if (TCPFlag) {
						s = bookClient.sendAndRecieveTCP(hostAddress, cmd, tcpPort);
					}
					else {
						s = bookClient.sendAndReceiveUDP(ia, datasocket, cmd, udpPort);
					}
				} else {
					System.out.println("ERROR: No such command");
				}
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private static void write(String s, int clientId) {
		try {
			String filename = "out_" + clientId + ".txt";
		    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
			fw.write(s);
			fw.close();
		} catch(IOException e) {
			System.err.println("IOException: " + e.getMessage());
		}
	}

	public String sendAndReceiveUDP(InetAddress ia, DatagramSocket datasocket, String cmd, int port) {
		String retstring = new String("");
		try {
			byte[] buffer = new byte[cmd.length()];
			buffer = cmd.getBytes();
			DatagramPacket sPacket = new DatagramPacket(buffer, buffer.length, ia, port);
			datasocket.send(sPacket);
			byte[] rbuffer = new byte[2048];
			DatagramPacket rPacket = new DatagramPacket(rbuffer, rbuffer.length);
			datasocket.receive(rPacket);
			retstring = new String(rPacket.getData(), 0, rPacket.getLength());
		}
		catch (IOException e){
			e.printStackTrace();
		}
		return retstring;
	}
	
	public String sendAndRecieveTCP(String nameServer, String cmd, int port) {
		String retstring = new String("");
		try {
			getSocket(nameServer, port);
			pout.println(cmd);
			pout.flush();
			while (din.hasNextLine()){
				retstring += din.nextLine() + "\n";
			}
			server.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return retstring;
	}
	
	public void getSocket(String nameServer, int serverPort) throws IOException {
		server = new Socket(nameServer, serverPort);
		din = new Scanner(server.getInputStream());
		pout = new PrintStream(server.getOutputStream());
	}
}