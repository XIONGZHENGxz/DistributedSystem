import java.io.*;
import java.util.*;
import java.net.*;
public class BookClient {
	private DatagramSocket udpIn;
	private DatagramSocket udpOut;
	private PrintWriter fileWriter;
	private Scanner tcpIn;
	private PrintStream tcpOut;
	private int clientId;
	private InetAddress ia;
	private int tcpPort;
	private int udpOutPort;
	private int udpInPort;
	private boolean udpMode;
	private boolean connect;

	public static void main (String[] args) {
		if (args.length != 2) {
			System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
			System.out.println("\t(1) <command-file>: file with commands to the server");
			System.out.println("\t(2) client id: an integer between 1..9");
			System.exit(-1);
		}

		String cmdFile = args[0];
		String hostAddress = "localhost";
                                                                                 //TCP   UDP
		BookClient client = new BookClient(Integer.parseInt(args[1]), hostAddress, 7000, 8000);

		Scanner sc = null;
		try {
			sc = new Scanner(new FileReader(cmdFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		while(sc.hasNextLine()) {
			String cmd = sc.nextLine();
			String[] tokens = cmd.split(" ");

			client.sendMessage(cmd);

			if (tokens[0].equals("setmode")) {
				if(tokens[1].equals("U"))
					client.setUdpMode(true);
				else if(tokens[1].equals("T"))
					client.setUdpMode(false);
			} else if (tokens[0].equals("exit")) {
				client.shutDown();
			} else if(tokens[0].equals("inventory")){
				int numMsgs = Integer.parseInt(client.getMessage());
				for(int i=0; i<numMsgs; i++)
					client.writeOutputFile(client.getMessage());
			} else if(!tokens[0].equals("return") && !tokens[0].equals("list") 
						&& !tokens[0].equals("borrow")){
				System.out.println("ERROR: No such command");
			} else{
				client.writeOutputFile(client.getMessage());
			}
		}
		sc.close();

	}

	public BookClient(int clientId, String hostAddress, int tcpPort, int udpPort){
		this.clientId = clientId;
		try{
			this.ia = InetAddress.getByName(hostAddress);
		} catch(UnknownHostException e){
			System.out.println("Client " + clientId + " UnknownHostException");
		} catch(SecurityException e){
			System.out.println("Client " + clientId + " SecurityException");
		}
		this.tcpPort = tcpPort;
		this.udpOutPort = udpPort;
		udpInPort = udpPort + clientId;
		udpMode = true;
		connect = false;
		try {
			udpIn = new DatagramSocket(udpInPort);
			udpOut = new DatagramSocket();
			fileWriter = new PrintWriter(new File("out_" + Integer.toString(clientId) + ".txt"));
		} catch (Exception e) {
			System.out.println("Datagram socket/fileWriter exception");
		}
	}

	public void setUdpMode(boolean mode){
		getMessage();
		udpMode = mode;
	}
	
	public void setConnect(boolean c) {
		connect = c;
	}
	
	public boolean getConnect() {
		return connect;
	}

	public void sendMessage(String msg){
		if(udpMode)
			sendUdpMessage(msg);
		else
			sendTcpMessage(msg);
	}

	private void sendUdpMessage(String msg){
		try{
			msg = Integer.toString(clientId) + " " + msg;
			byte[] buf = new byte[msg.length()];
			buf = msg.getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, ia, udpOutPort);
			udpOut.send(packet);
		} catch(Exception e){
			System.out.println("Client UDP Send Exception");	
		}
	}

	private void sendTcpMessage(String msg){
		if(!getConnect()){
			Socket server = null;
			try{
				server = new Socket(InetAddress.getLocalHost(), tcpPort);
			} catch (ConnectException e){
				System.out.println("Nobody is listening");
			}
			catch(Exception e){
				System.out.println("Client socket setup exception");
			}
			try {
				tcpIn = new Scanner(server.getInputStream());
				tcpOut = new PrintStream(server.getOutputStream());
			} catch (IOException e) {
				System.out.println("tcpIn/Out initialization exception");
			}
			setConnect(true);
		}
		
		tcpOut.println(msg);
		tcpOut.flush();
	}

	public String getMessage(){
		if(udpMode)
			return getUdpMessage();
		else
			return getTcpMessage();
	}

	private String getUdpMessage(){
		byte[] buf = new byte[1024];
		DatagramPacket datapacket = new DatagramPacket(buf, buf.length);
		try {
			udpIn.receive(datapacket);
		} catch (IOException e) {
			System.out.println("Client error receiving message");
		}
		return new String(datapacket.getData(), 0, datapacket.getLength());
	}

	private String getTcpMessage(){
		return tcpIn.nextLine();
	}

	public void writeOutputFile(String msg){
		fileWriter.println(msg);
		fileWriter.flush();
	}
	
	public void shutDown() {
		try{
			Thread.sleep(10);
		} catch(InterruptedException e){
			System.out.println("sleep interrupted");
		}
		udpIn.close();
		udpOut.close();
		fileWriter.close();
		if(getConnect()){
			tcpIn.close();
			tcpOut.close();
		}
	}
}
