import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.io.PrintWriter;
import java.net.*;
import java.io.*;

/*
 * EID's of group members
 * mrs4239
 * khs562
 */

public class BookClient {
	
	public static void main(String args[]) {
		// Get args
		if (args.length != 2) {
		  System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
		  System.out.println("\t(1) <command-file>: file with commands to the server");
		  System.out.println("\t(2) client id: an integer between 1..9");
		  System.exit(-1);
		}
		String commandFileName = args[0];
		String clientId = args[1];
		
		// Create client
		BookClient client = new BookClient(commandFileName, clientId);
		client.runCommands();
	}
	
	
	private String commandFileName;
	private String id;
	private boolean exited = false;

	private boolean useTCP = false; // use UPD by default
	private InetAddress ia;
	private byte[] buffer = new byte[Settings.MAX_BUFF_LEN];
	private DatagramSocket socketUDP;
	private DatagramPacket sendPacket, receivePacket;
	private Integer port_UDP = Settings.UPD_PORT;
	private Socket clientSocket;
	private DataOutputStream outToServer;
	private BufferedReader inFromServer;
	
	public BookClient(String commandFileName, String id) {
		this.commandFileName = commandFileName;
		this.id = id;
	}
	
	public void runCommands() {
		String filename = new String("out_"+this.id+".txt");
		try{
		PrintWriter writer = new PrintWriter(filename, "UTF-8");

		try {
			// Open command file
			Scanner sc = new Scanner(new FileReader(commandFileName));
			// Open UDP/TCP sockets
			openSockets();
			// Parse/run commands
			while (!exited && sc.hasNextLine()) {
				String cmd = sc.nextLine();
				if(handleCommand(cmd))
					continue;
				//System.out.println("Client cmd:"+cmd);
				send(cmd);
				if(cmd.equals("exit")) break;
				
				String response = receive();
				//System.out.println("Response: " + response);
				writer.println(response);
			}
			writer.close();
			closeSockets();
		} catch (Exception e) {
			System.err.println(e);
		}
		}catch(Exception e){
			System.err.println(e);
		}
	}
	
	private void openSockets() throws UnknownHostException, SocketException {
		ia = InetAddress.getByName(Settings.HOSTNAME);
		socketUDP = new DatagramSocket();
        try{
		
		clientSocket = new Socket(Settings.HOSTNAME,Settings.TCP_PORT);
        outToServer = new DataOutputStream(clientSocket.getOutputStream());
        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }catch(Exception e){}
		
	}
	
	private void send(String msg) throws IOException {
		if (useTCP) {
			buffer = msg.getBytes();
			String str = new String(buffer);
			str += "\n";
			outToServer.writeBytes(str);
		} else {
			if (port_UDP == Settings.UPD_PORT) { // request communication port
				buffer = "setmode U".getBytes();
				sendPacket = new DatagramPacket(buffer, buffer.length, ia, port_UDP);
				socketUDP.send(sendPacket);
				String response = receive();
				port_UDP = Integer.parseInt(response);
			}
			buffer = msg.getBytes();
			sendPacket = new DatagramPacket(buffer, buffer.length, ia, port_UDP);
			socketUDP.send(sendPacket);
		}
	}
	
	private String receive() throws IOException {
		String response = "";
		if (useTCP) {
			 String incomming = "";
			do{
	        incomming = inFromServer.readLine();
	        if(!incomming.equals("DONE") && incomming.length()>0)
	        	response += incomming + "\n";
			}while(!incomming.equals("DONE"));
			response = response.substring(0, response.length()-1);
	        //System.out.println("FROM SERVER:" + response);
		} else {
			//Added this to clear buffer
			buffer = new byte[Settings.MAX_BUFF_LEN];
			
			receivePacket = new DatagramPacket(buffer, buffer.length);
			socketUDP.receive(receivePacket);
			response = new String(receivePacket.getData());
			
			String s = new String();
			for(int i=0;i<response.length();i++){
				if(response.charAt(i) != 0)
					s += response.substring(i,i+1);
			}
			response = s;
		}
		return response;
	}
	
	private void closeSockets() {
		socketUDP.close();
		try{
        clientSocket.close();
		} catch (Exception e){
			
		}		
	}
	
	/**
	 * If command is 'setmode', set communications to UDP/TCP accordingly.
	 * If command is 'exit', stop parsing the command file.
	 * 
	 * Returns true if this command is handled by client and should not be sent
	 * to the server.
	 */
	private boolean handleCommand(String cmd) {
		cmd = cmd.toLowerCase();
		// handle setmode
		if (cmd.length() >= "setmode".length() && cmd.startsWith("setmode")) {
			if (cmd.endsWith("t"))
				useTCP = true;
			else {
				useTCP = false;
			}
			return true;
		}
		// handle exit
		if (cmd.length() >= "exit".length() && cmd.startsWith("exit")) {
			exited = true;
		}
		return false;
	}
	
}
