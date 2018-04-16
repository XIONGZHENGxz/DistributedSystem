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
    
    boolean udp = true;
    BufferedWriter writer = null;
    
    try {
    	
        Socket clientSocket = null;
    	PrintStream outToServer = null;
        Scanner inFromServer = null;
        
        InetAddress ia = InetAddress.getByName(hostAddress);
        DatagramSocket dataSocket = new DatagramSocket();
        
    	writer = new BufferedWriter(new FileWriter("out_" + clientId + ".txt"));

        Scanner sc = new Scanner(new FileReader(commandFile));

        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");
          String response;


          if (tokens[0].equals("setmode")) {
            // TODO: set the mode of communication for sending commands to the server
        	  if(tokens[1].equals("T")) {
        		  
        		  udp = false;
        		  
        		  clientSocket = new Socket(hostAddress, tcpPort);
				  outToServer = new PrintStream(clientSocket.getOutputStream(), true);
				  inFromServer = new Scanner(clientSocket.getInputStream());
        		  
        	  } else {
        		  
        		  udp = true; 
        		  
        		  dataSocket = new DatagramSocket();
        		  
        	  }
       
          } else {
        	 
        	  String send = clientId + " " + cmd;
        	  if(udp) {
        		  byte[] b = send.getBytes();
        		  DatagramPacket sPacket = new DatagramPacket(b,b.length,ia,udpPort);
        		  dataSocket.send(sPacket);
        		  byte[] rBuffer = new byte[4096]; // 8192

        		  DatagramPacket rPacket = new DatagramPacket(rBuffer, rBuffer.length);
        		  dataSocket.receive(rPacket);
        		  response = new String(rPacket.getData(), 0, rPacket.getLength ());
        		  
        	  } else {


        		  outToServer.println(send);
        		  outToServer.flush();
        		  while(!inFromServer.hasNext()){}
        		  response = inFromServer.nextLine();

        	  }
        	  if(!tokens[0].equals("exit")) {
        	  	if(tokens[0].equals("inventory") || tokens[0].equals("list")){
        	  		String[] responseArr = response.split("%%%");
        	  		for (String str: responseArr) {
						writer.write(str);
						writer.newLine();
					}
        	  	}
        	  	else{
					  writer.write(response);
					  writer.newLine();
        	  	}
        	  }
        	  else {
				  if (udp) {
					  dataSocket.close();
				  } else {
					  clientSocket.close();
				  }
			  }
          }
          
        }
        
        writer.close();
        
    } catch (FileNotFoundException e) {
    	e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
  }
  
  
  
}
