import java.io.FileReader;
import java.util.Scanner;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.PrintWriter;;
import java.net.*;

/*
 * EID's of group members
 * mrs4239
 * khs562
 */

public class BookServer {

	public static void main(String[] args) {
		// Read in arg
		if (args.length != 1) {
		  System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
		  System.exit(-1);
		}
		String fileName = args[0];
		
		// Parse inventory file
		try {
			Scanner sc = new Scanner(new FileReader(fileName));
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				line = line.trim();
				int splitIndex = line.lastIndexOf(" ");
				String bookName = line.substring(0, splitIndex);
				int quantity = Integer.parseInt( line.substring(splitIndex+1) );
				library.insert(bookName, quantity);
			}
		} catch (Exception e) {
			System.err.println(e);
		}
		
		// Startup request listener threads to handle client requests
		BookServer server = new BookServer();
		Thread updListener = server.new UPDConnectionThread(server);
		Thread tcpListener = server.new TCPThread(server);
		updListener.start();
		tcpListener.start();
		try {
			updListener.join();
			tcpListener.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	private static Library library = new Library();
	
	/**
	 * Used to tell the TCP/UDP listeners that the client requested an end to
	 * their communication.
	 */
	private final static String EXIT_IDENTIFIER = ":::EXIT:::";

	/**
	 * If line contains cmd, return line with cmd removed, else null
	 */
	private static String isCommand(String line, String cmd) {
		line = line.trim();
		if (line.length() < cmd.length() || !line.startsWith(cmd))
			return null;
		return line.substring(cmd.length()).trim();
	}
	
	/**
	 * Parse and process the command from client thread. Returns the response
	 * message. If no response is required, returns null. If the command was
	 * an exit, returns EXIT_IDENTIFIER so the client thread can respond as
	 * needed.
	 * @param cmd
	 * @return response
	 */

	private static String processCommand(String cmd) {
		
		cmd = cmd.trim();
		String params;
		if ((params = isCommand(cmd, "borrow")) != null) {

			int splitIndex = params.indexOf('"');
			String studentName = params.substring(0, splitIndex).trim();
			String bookName = params.substring(splitIndex).trim();
			return library.borrow(studentName, bookName);
		} else if ((params = isCommand(cmd, "return")) != null) {
			return library.returnBook(Integer.parseInt(params));
		} else if ((params = isCommand(cmd, "inventory")) != null) {
			return library.inventory();
		} else if ((params = isCommand(cmd, "list")) != null) {
			return library.list(params);
		} else if ((params = isCommand(cmd, "exit")) != null) {
			try{
			PrintWriter writer = new PrintWriter("inventory.txt", "UTF-8");
			writer.println(library.inventory());
			writer.close();
			}
			catch(Exception e){
				System.err.println(e);
			}
			return EXIT_IDENTIFIER;
		} else {
			return "ERROR: No such command";
		}
	}
	
	
	
	/**
	 * Polls UPD_PORT for setmode commands, then returns the port to continue
	 * communication on.
	 */
	class UPDConnectionThread extends Thread {
		private int comm_port = Settings.UPD_PORT + 1; 
		private BookServer server;
		public UPDConnectionThread(BookServer server) { this.server = server; }

		@Override
		public void run() {
			DatagramPacket receivePacket, returnpacket;
			try {
				DatagramSocket socketUDP = new DatagramSocket(Settings.UPD_PORT);
				byte[] buffer = new byte[Settings.MAX_BUFF_LEN];
				while (true) {
					buffer = new byte[Settings.MAX_BUFF_LEN];
					// Receive command packet
					receivePacket = new DatagramPacket(buffer, buffer.length);
					socketUDP.receive(receivePacket);
					// Parse command
					String cmd = new String(receivePacket.getData());
					
					String s = new String();
					for(int i=0;i<cmd.length();i++){
						if(cmd.charAt(i) != 0)
							s += cmd.substring(i,i+1);
					}
					cmd = s;
					
					//System.out.println("Connection"+ cmd);
					if (cmd.length() < "setmode".length() || !cmd.startsWith("setmode")) continue;
					
					// Get communication port and spawn thread for it
					comm_port++;
					String response = new String(Integer.toString(comm_port));
					Thread t = new UPDCommandThread(server, comm_port);
					t.start();
					// Send response (if needed)
					for(int i=0;i<Settings.MAX_BUFF_LEN;i++)
						buffer[i] = 0;
					buffer = response.getBytes();
					
					returnpacket = new DatagramPacket(
							buffer,
							buffer.length,
							receivePacket.getAddress(),
							receivePacket.getPort());
					socketUDP.send(returnpacket);
				}
			} catch (Exception e) {
				System.err.println("BookServer UDP error: " + e);
			}
		}
	}

	class UPDCommandThread extends Thread {
		private BookServer server;
		private int comm_port;
		public UPDCommandThread(BookServer server, int comm_port) {
			this.server = server;
			this.comm_port = comm_port;
		}

		@Override
		public void run() {
			DatagramPacket receivePacket, returnpacket;
			try {
				DatagramSocket socketUDP = new DatagramSocket(comm_port);
				byte[] buffer = new byte[Settings.MAX_BUFF_LEN];
				while (true) {
					// Receive command packet
					receivePacket = new DatagramPacket(buffer, buffer.length);
					socketUDP.receive(receivePacket);

					// Parse command
					String cmd = new String(receivePacket.getData(), 0, receivePacket.getLength());
					//System.out.println("Server got Client msg: "+cmd);
					String response = server.processCommand(cmd);

					if (response.equals(EXIT_IDENTIFIER))
						break;

					// Send response (if needed)
					if (response != null && !response.equals(EXIT_IDENTIFIER)) {
						//System.out.println("Server to Client: "+response);
						buffer = response.getBytes();

						returnpacket = new DatagramPacket(
								buffer,
								buffer.length,
								receivePacket.getAddress(),
								receivePacket.getPort());
						socketUDP.send(returnpacket);
					}
				}
				socketUDP.close();
			} catch (Exception e) {
				System.err.println("BookServer UDP error: " + e);
			}
		}
	}
	
	
	
	class TCPThread extends Thread {
		private BookServer server;
		Socket theClient;
		public TCPThread(BookServer server) { this.server = server; }
		
		@Override
		public void run() {
			try {
				// TODO setup TCP
				byte[] buffer = new byte[Settings.MAX_BUFF_LEN];
				while (true) {
					
					try {
						ServerSocket serverSocket = new ServerSocket(Settings.TCP_PORT);
						Socket s;
						while ( (s = serverSocket.accept()) != null) {
							Thread t = new TCPCommandThread(s);
							t.start();
						}
					} catch (Exception e) {
						System.err.println("Server aborted:" + e);
					}
					
					
					// TODO listen for messages and create thread to process them (see NameServer.java for example)
				}
			} catch (Exception e) {
				System.err.println("BookServer TCP error: " + e);
			}
		}
	}
	
	class TCPCommandThread extends Thread{
		private Socket theClient;
		public TCPCommandThread(Socket s){
			theClient = s;
		}
		public void run() {
			try {
				while(true){
				Scanner sc = new Scanner(theClient.getInputStream());
				PrintWriter pout = new PrintWriter(theClient.getOutputStream());
				String command = "";
				
				if(sc.hasNext() == false)
					break;
				
				command = sc.nextLine();

				if (command.equals(EXIT_IDENTIFIER))
					break;
				//System.out.println("received:" + command);
				String output = processCommand(command);
				//System.out.println("server2client:" + output);

				if (output.equals(EXIT_IDENTIFIER))
					break;
				
				if(output.length()>0){
					String lines[] = output.split("\\r?\\n");
					for(int i=0;i<lines.length;i++)
						pout.println(lines[i] + "\n");
					
					pout.println("DONE");
				}
				
				pout.flush();
				}
				theClient.close();
			} catch (Exception e) {
				System.err.println(e);
			}

		}
		
	}
	
	

}
