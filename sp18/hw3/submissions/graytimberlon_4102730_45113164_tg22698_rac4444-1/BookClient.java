import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
public class BookClient {
	static PrintWriter out = null;
	static BufferedReader in = null;
	static DatagramPacket receivepacket = null;
	static DatagramPacket sendpacket = null;
	static DatagramSocket datasocket = null;
	static byte[] buf = new byte[1024];
	static boolean is_TCP = false;
	public static void main (String[] args) {
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

		String inventoryInput = fileToString(commandFile);
		String[] lines = inventoryInput.split("\\r?\\n");
		Socket socket = null;
		
		// UDP setup (since it is default)
		try {
			datasocket = new DatagramSocket(0);
			receivepacket = new DatagramPacket(buf, buf.length);
			sendpacket = new DatagramPacket(buf, buf.length,
					InetAddress.getByName(hostAddress), udpPort);
		} catch (Exception e) {
			System.out.println("Error setting up UDP socket.");
			e.printStackTrace();
		}
		

		for (String cmd : lines) {
			String[] tokens = cmd.split(" ");
			
			if (tokens[0].equals("setmode")) {
				if (tokens[1].equals("T")) {
					//TCP
					try {
						socket = new Socket(InetAddress.getLocalHost().getHostAddress(), tcpPort);
						out = new PrintWriter(socket.getOutputStream());
						in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						is_TCP = true;
						//System.out.println("set mode to TCP");
					} catch (Exception e) {
						//System.out.println("Error setting up TCP socket.");
						e.printStackTrace();
					}
				} else { //UDP (already set up)
                    is_TCP = false;
				}
			}
			else if (tokens[0].equals("borrow")) {
				String p_cmd = processCommand(cmd);
				System.out.println(p_cmd);
				printToFile("src/out_" + clientId + ".txt", p_cmd);
			} else if (tokens[0].equals("return")) {
				String p_cmd = processCommand(cmd);
				System.out.println(p_cmd);
				printToFile("src/out_" + clientId + ".txt", p_cmd);
			} else if (tokens[0].equals("inventory")) {
				String p_cmd = processCommand(cmd);
				System.out.println(p_cmd);
				printToFile("src/out_" + clientId + ".txt", p_cmd);
			} else if (tokens[0].equals("list")) {
				String p_cmd = processCommand(cmd);
				System.out.println(p_cmd);
				printToFile("src/out_" + clientId + ".txt", p_cmd);
			} else if (tokens[0].equals("exit")) {
				processCommand(cmd);
			} else {
				System.out.println("ERROR: No such command");
			}
		}
		System.exit(0);
	}
	
	public static String processCommand(String cmd) {
		if (is_TCP) {
			out.println(cmd);
			out.flush();
			String msg = "";
			try {
				while ((msg = in.readLine()) == null) {}
				msg = msg.replaceAll("&carrot@!666", "\n");
				return msg;
			} catch (IOException e) {
				//System.out.println("Error sending emssage over TCP");
				e.printStackTrace();
			}
		} else {
			// UDP process
			try {
				buf = cmd.getBytes();
				sendpacket = new DatagramPacket(buf, buf.length, 
						sendpacket.getAddress(), sendpacket.getPort());
				datasocket.send(sendpacket);
	            datasocket.receive(receivepacket);
	            String msg = new String(receivepacket.getData(), 
	                    receivepacket.getOffset(), receivepacket.getLength());
	            msg = msg.replaceAll("&carrot@!666", "\n");
	            return msg;
			} catch (Exception e) {
				//System.out.println("Error sending emssage over UDP");
				e.printStackTrace();
			}
		}
		return "Error in processCommand";
	}
	
    public static String fileToString(String resource) {
    	try(BufferedReader br = new BufferedReader(new FileReader("src/" + resource))) {
    	    StringBuilder sb = new StringBuilder();
    	    String line = br.readLine();

    	    while (line != null) {
    	        sb.append(line);
    	        sb.append(System.lineSeparator());
    	        line = br.readLine();
    	    }
    	    return sb.toString();
    	} catch(Exception e) {
    		return "";
    	}
    }
    
    public static void printToFile(String resource, String str) {
		try {
		FileWriter fw = new FileWriter(resource, true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(str);
		bw.newLine();
		bw.flush();
		if (bw!=null) bw.close();
		if (fw!=null) fw.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
}
     
}

