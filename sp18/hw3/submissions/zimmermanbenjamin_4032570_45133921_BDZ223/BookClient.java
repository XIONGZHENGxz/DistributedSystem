import java.io.*;
import java.util.*;
import java.net.*;

public class BookClient {
	String hostAddress;
	int tcpPort;
	int udpPort;
	int clientId;
	String mode = "U";
	String outfile;
	PrintWriter fileWriter;

	//UDP variables
	InetAddress ia;
	DatagramSocket datasocket;
	DatagramPacket sPacket, rPacket;

	//TCP variables
	Socket server;
	Scanner din;
	PrintStream pout;
  public static void main(String[] args) {
    if(args.length != 2) {
      System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
      System.out.println("\t(1) <command-file>: file with commands to the server");
      System.out.println("\t(2) client id: an integer between 1..9");
      System.exit(-1);
    }

		BookClient client = new BookClient();

    String commandFile = args[0];
    client.clientId = Integer.parseInt(args[1]);
    client.hostAddress = "localhost";
    client.tcpPort = 7000;// hardcoded -- must match the server's tcp port
    client.udpPort = 8000;// hardcoded -- must match the server's udp port

		client.outfile = "out_" + client.clientId + ".txt";

		client.setmode("U");

    try {
			Scanner sc = new Scanner(new FileReader(commandFile));
			client.fileWriter = new PrintWriter(client.outfile);
			while(sc.hasNextLine()) {
				String cmd = sc.nextLine();
				String[] tokens = cmd.split(" ");

				if(tokens[0].equals("setmode")) {
					if(tokens[1].equals("T")) {
						if(client.mode.equals("U")) {
							client.setmode("T");
						}
					}
					else if(tokens[1].equals("U")) {
						if(client.mode.equals("T")) {
							client.setmode("U");
						}
					}
					else System.out.println("ERROR: setmode only accepts arguments U and T");
				} 
				else if(tokens[0].equals("borrow")) {
					client.sendCommand(cmd);
				} 
				else if(tokens[0].equals("return")) {
					client.sendCommand(cmd);
				} 
				else if(tokens[0].equals("inventory")) {
					client.sendCommand(cmd);
				} 
				else if(tokens[0].equals("list")) {
					client.sendCommand(cmd);
				} 
				else if(tokens[0].equals("exit")) {
					client.sendCommand(cmd);
					break;
				} 
				else {
					System.out.println("ERROR: No such command");
				}
			}
    } 
		catch(FileNotFoundException e) {
			e.printStackTrace();
    }
  }

	private void setmode(String mode) {
		this.mode = mode;
		if(mode.equals("U")) {
			try {
				ia = InetAddress.getByName(hostAddress);
				datasocket = new DatagramSocket();
			}
			catch(UnknownHostException e) {
				System.err.println(e);
			}
			catch(SocketException e) {
				System.err.println(e);
			}
		}
		else if(mode.equals("T")) {
			try {
				server = new Socket(hostAddress, tcpPort);
				din = new Scanner(server.getInputStream());
				pout = new PrintStream(server.getOutputStream());
			}
			catch(IOException e) {
				System.err.println(e);
			}
		}
	}

	private void sendCommand(String cmd) {
		if(mode.equals("U")) {
			try {
				boolean exiting = cmd.equals("exit");
				cmd = cmd.length() + "\n"	+ cmd;
				byte[] buffer = new byte[cmd.length()];
				byte[] rbuffer = new byte[1024];
				buffer = cmd.getBytes("UTF-8");
				int index = 0;
				sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
				datasocket.send(sPacket);
				if(!exiting) {
					rPacket = new DatagramPacket(rbuffer, rbuffer.length);
					datasocket.receive(rPacket);
					String retstring = new String(rPacket.getData(), 0, rPacket.getLength());
					fileWriter.println(retstring);
					fileWriter.flush();
				}
			}
			catch(IOException e) {
				System.err.println(e);
			}
		}
		else if(mode.equals("T")){
			pout.println(cmd);
			pout.flush();
			int lines = Integer.valueOf(din.nextLine());
			String retstring = "";
			if(lines > 0) {
				retstring = din.nextLine();
				for(int i = 1; i < lines; i++) {
					retstring += "\n" + din.nextLine();
				}
			}
			fileWriter.println(retstring);
			fileWriter.flush();
		}
	}
}
