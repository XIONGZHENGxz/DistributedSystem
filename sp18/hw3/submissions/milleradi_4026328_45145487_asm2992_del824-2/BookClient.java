import java.io.*;
import java.util.*;
import java.net.*;
public class BookClient {
	Scanner data_in;
	PrintWriter data_out;
	PrintStream pout;
	Socket server;
	InetAddress ia;
	DatagramSocket dataSocket;
	DatagramPacket sendPacket, receivePacket;
	byte[] rBuff = new byte[65507];
  public static void main (String[] args) throws IOException{
    String hostAddress;
    int tcpPort;
    int udpPort;
    int clientId;
    boolean mode=true;
    boolean isFirstUDP = true;
    boolean isFirstTCP=true;
    
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
          System.out.println(cmd);
          String[] tokens = cmd.split(" ");

          if (tokens[0].equals("setmode")) {
            // TODO: set the mode of communication for sending commands to the server 
        	  if(tokens[1].equals("U")){
        		  mode=true;
        		  System.out.println("UDP");

        	  }
        	  else if(tokens[1].equals("T")){
        		  mode=false;
        		  System.out.println("TCP");

        	  }
        	  else{
        		  System.out.println("invalid mode");
        	  }
          }
          else {
        	  if(mode==true){
        		  if(isFirstUDP==true){
        			  System.out.println("First UDP- sending to server");
        			  client.sendToServerUDP(cmd + " " + Integer.toString(clientId), poutFile, clientId, udpPort, isFirstUDP);
          			  isFirstUDP = false;
        		  }
        		  else{
        			  client.sendToServerUDP(cmd + " " + Integer.toString(clientId), poutFile, clientId, udpPort, isFirstUDP);
        		  }
        	  }
        	  else{
        		  if(isFirstTCP==true){
        			  client.getTCPSocket(hostAddress, tcpPort);
            		  isFirstTCP = false;
                      try {
						client.sendToServerTCP(cmd, poutFile);
					} catch (NoSuchElementException | InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		  }
        		  else{
        			  try {
						client.sendToServerTCP(cmd, poutFile);
					} catch (NoSuchElementException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		  }
        		  
        	  }
        	  
          }
        }
    }catch (UnknownHostException e) {
	// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  
  }
  
  public void getUDPSocket(String host) throws IOException {
	  System.out.println("got UDP socket");
	  dataSocket = new DatagramSocket();
	  ia =	InetAddress.getByName(host);
  }
  
  public void getTCPSocket(String host, int port) throws IOException {
	  server = new Socket(host , port);
	  data_in =	new Scanner(server.getInputStream());
	  data_out = new PrintWriter(server.getOutputStream());
  }
  
  public void sendToServerUDP(String message, PrintWriter poutFile, int clientId, int udpPort, boolean first) throws IOException{
	  System.out.println("UDP send to server function");
		byte[] buffer = new byte[65507]; //65507
		buffer = message.getBytes();
		sendPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
		dataSocket.send(sendPacket);
		receivePacket = new DatagramPacket(rBuff, rBuff.length, ia, 8000 + clientId);
		dataSocket.receive(receivePacket);
		System.out.println(receivePacket.getData().toString());
		String  returnStg = new String(receivePacket.getData(), 0, receivePacket.getLength());
		if(returnStg.equals("0")){
			System.exit(0);
		}
		if(first){
			System.out.println(returnStg);
			poutFile.print(returnStg+"\n");
			poutFile.flush();
		}
		else if(returnStg.equals("\n") == false){
			System.out.println(returnStg+"\n");
			poutFile.print(returnStg);
			poutFile.flush();
		}
		return;
		
	}
  
  public void sendToServerTCP(String message, PrintWriter poutFile) throws InterruptedException, NoSuchElementException{
		
		data_out.println(message);

		data_out.flush();
		String lines = data_in.nextLine();
		if(lines.equals("0")){
			System.exit(0);
		}
		
		int line_num = Integer.parseInt(lines);

		
		for(int i=0; i<line_num; i++){
			String input = data_in.nextLine();
			System.out.println(input);
			poutFile.println(input);
		}
		poutFile.flush();
	}
}
