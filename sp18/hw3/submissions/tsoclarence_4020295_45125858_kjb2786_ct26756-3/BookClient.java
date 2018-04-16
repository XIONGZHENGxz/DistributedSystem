import java.util.Scanner;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
public class BookClient {
	Scanner din;
	PrintWriter dout;
	PrintStream pout;
	Socket server;
	InetAddress ia;
	DatagramSocket dataSocket;
	DatagramPacket sPacket, rPacket;
	byte[] rBuff = new byte[65507];
  public static void main (String[] args) throws IOException, InterruptedException {
    String hostAddress;
    int tcpPort;
    int udpPort;
    int clientId;
    
    boolean TCPup = false;
    boolean isFirstUDP = true;
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
	
	String fileName = "out_" + clientId + ".txt";
	File file = new File(fileName);
	PrintWriter poutFile = new PrintWriter(new FileWriter(file));

    try {
        Scanner sc = new Scanner(new FileReader(commandFile));
        BookClient client = new BookClient();
        client.getUDPSocket(hostAddress);
        
        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");

          if (tokens[0].equals("setmode")) {
            // TODO: set the mode of communication for sending commands to the server 
        	 if(tokens[1].equals("T")){
        		 isUDP = false;
        	 }
        	 else{
        		 isUDP = true;
        	 }
          }
          else{
        	  if(isUDP == true){
        		if(isFirstUDP ==  true){  
        			client.sendToServerUDP(cmd + " " + Integer.toString(clientId), poutFile, clientId);
        			isFirstUDP = false;
        		}
        		else{
        			client.sendToServerUDPNext(cmd + " " + Integer.toString(clientId), poutFile, clientId);
        		}
        		
        	  }
              else{
            	  if(TCPup == false){
            		  client.getTCPSocket(hostAddress, tcpPort);
            		  TCPup = true;
                      client.sendToServerTCP(cmd, poutFile);

            	  }
            	  else{
                   	client.sendToServerTCP(cmd, poutFile);

            	  }
              }
          }
        	  

        }
    } catch (FileNotFoundException e) {
	e.printStackTrace();
    }
  }
  public void getTCPSocket(String host, int port) throws IOException {
	  server = new Socket(host , port);
	  din =	new Scanner(server.getInputStream());
	  
	  dout = new PrintWriter(server.getOutputStream());
	 // pout = new PrintStream(server.getOutputStream());
  }
  public void getUDPSocket(String host) throws IOException {
	  dataSocket = new DatagramSocket();
	  ia =	InetAddress.getByName(host);
  }
  
	public void sendToServerUDP(String message, PrintWriter poutFile, int clientId) throws IOException{
		
		byte[] buffer = new byte[65507]; //65507
		buffer = message.getBytes();
		sPacket = new DatagramPacket(buffer, buffer.length, ia, 8000);
		dataSocket.send(sPacket);
		rPacket = new DatagramPacket(rBuff, rBuff.length, ia, 8000 + clientId);
		dataSocket.receive(rPacket);
		String  retstring = new String(rPacket.getData(), 0, rPacket.getLength());
		if(retstring.equals("0")){
			System.exit(0);
		}
		System.out.println(retstring);
		
		poutFile.print(retstring);
		poutFile.flush();
		
	}
	
	
public void sendToServerUDPNext(String message, PrintWriter poutFile, int clientId) throws IOException{
		
		byte[] buffer = new byte[65507]; //65507
		buffer = message.getBytes();
		sPacket = new DatagramPacket(buffer, buffer.length, ia, 8000 + clientId);
		dataSocket.send(sPacket);
		rPacket = new DatagramPacket(rBuff, rBuff.length, ia, 8000 + clientId);
		dataSocket.receive(rPacket);
		String  retstring = new String(rPacket.getData(), 0, rPacket.getLength());
		if(retstring.equals("0")){
			System.exit(0);
		}
		if(retstring.equals("\n") == false){
		
			System.out.println(retstring);
			poutFile.println(retstring);
			poutFile.flush();
		}
		
	}
	
	
	public void sendToServerTCP(String message, PrintWriter poutFile) throws InterruptedException, NoSuchElementException{
		
		dout.println(message);

		dout.flush();
		String lines = "";
		lines = din.nextLine();
		int lineNum;
		if(lines.equals("0")){
			System.exit(0);
		}
		
		lineNum = Integer.parseInt(lines);

		
		for(int i=0; i<lineNum; i++){
			String input = din.nextLine();
			System.out.println(input);
			poutFile.println(input);
		}
		poutFile.flush();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}