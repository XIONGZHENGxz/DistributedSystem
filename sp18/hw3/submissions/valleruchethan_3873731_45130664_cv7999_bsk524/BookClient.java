import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;

public class BookClient {
	private static int length = 1024;
	private static String hostAddress = "localhost";
    private static int tcpPort = 7000;// hardcoded -- must match the server's tcp port
    private static int udpPort = 8000;// hardcoded -- must match the server's udp port
	
    public static void main (String[] args) {
    	DatagramSocket uSocket = null;
    	Socket tSocket = null;
        InetAddress ia = null;
        int clientId;
    	String outputMessage = "";
        String currentProtocol = "UDP";

        if (args.length != 2) {
            System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
            System.out.println("\t(1) <command-file>: file with commands to the server");
            System.out.println("\t(2) client id: an integer between 1..9");
            System.exit(-1);
        }

        String commandFile = args[0];
        clientId = Integer.parseInt(args[1]);
         
        try {        	
            Scanner sc = new Scanner(new FileReader(commandFile));
            
            while(sc.hasNextLine()) {
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");

                if (tokens[0].equals("setmode")) {
                	if(tokens[1].equals("U")) {
                		currentProtocol = "UDP";
                	}
                	else if(tokens[1].equals("T")) {
                		currentProtocol = "TCP";
                	}
                }
                else if(tokens[0].equals("exit")){
                    writeOutputFile(clientId, outputMessage);
                    if(currentProtocol.equals("UDP")) {
                        handleUDP(tokens, cmd, udpPort, ia);
                    }
                    else if(currentProtocol.equals("TCP")) {
                        handleTCP(tokens, cmd);
                    }
                    //System.out.print("Exiting");
                    break;
                }
                else {
                	if(currentProtocol.equals("UDP")) {
                		outputMessage += handleUDP(tokens, cmd, udpPort, ia);
                	}
                	else if(currentProtocol.equals("TCP")) {
                		outputMessage += handleTCP(tokens, cmd) + "\n";
                	}
                }   
            }  
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /*
     * Handles the requests with via a UDP protocol
     */
    private static String handleUDP(String[] tokens, String command, int udpPort, InetAddress ia) throws IOException {
        DatagramSocket s = null;
        // open a datagram socket for UDP connections
        try {
            ia = InetAddress.getByName(hostAddress);
            s = new DatagramSocket();
        } catch(SocketException e) {
            System.err.println(e);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    	int port = udpPort;
    	byte[] sendBuffer = new byte[command.length()];
    	byte[] returnBuffer = new byte[length];
    	String returnString = "";

    	sendBuffer = command.getBytes();
    	DatagramPacket sendCommand = new DatagramPacket(sendBuffer, sendBuffer.length, ia, port);
    	s.send(sendCommand);
    	DatagramPacket returnMsg = new DatagramPacket(returnBuffer, returnBuffer.length);

    	if (tokens[0].equals("borrow") || tokens[0].equals("return") || tokens[0].equals("inventory") || tokens[0].equals("list") || (tokens[0].equals("exit"))) {
    		s.receive(returnMsg);
    		returnString = new String(returnMsg.getData(), 0, returnMsg.getLength());
    		//System.out.println("[UDP] Message received from Server : " + returnString);
        }
        else if (tokens[0].equals("exit")) {

        }
        else {
            System.out.println("ERROR: No such command");
        }
    	
    	return returnString;
    }
    
    /*
     * Handles the requests via a TCP protocol
     */
    private static String handleTCP(String[] tokens, String command) throws IOException {
        // open a socket for TCP connections
        Socket s = null;
        try {
            s = new Socket(hostAddress, tcpPort);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    	String returnString = "";
    	
    	DataOutputStream os = new DataOutputStream(s.getOutputStream());
    	os.writeBytes(command+"\r\n");
    	
    	InputStream is = s.getInputStream();
    	InputStreamReader isr = new InputStreamReader(is);
    	BufferedReader br = new BufferedReader(isr);
    	
    	if (tokens[0].equals("borrow") || tokens[0].equals("return") || tokens[0].equals("list") || (tokens[0].equals("exit"))) {
        	returnString = br.readLine();
        	//System.out.println("[TCP] Message received from Server : " + returnString);
        }
        else if(tokens[0].equals("inventory")){
            String line = null;
            line = br.readLine();
    	    while(line != null){
                returnString = returnString.concat(line + "\n");
                line = br.readLine();
            }
            returnString.trim();
            //System.out.print(returnString);
        }
        else {
            System.out.println("ERROR: No such command");
        }
    	
    	return returnString;
    }
    
    private static void writeOutputFile(int id, String m) {
    	String filename;
    	Writer writer;
    	
    	filename = "out_" + id + ".txt";
		try {
            Files.write(Paths.get(filename), m.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}





















