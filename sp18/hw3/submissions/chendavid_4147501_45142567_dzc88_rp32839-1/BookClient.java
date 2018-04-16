import java.util.Scanner;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.*;
public class BookClient {
	
  public static void main (String[] args) {
    String hostAddress;
    int tcpPort;
    int udpPort;
    int clientId;
    int len;
    char type = 'u';

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
    len = 1024;// hardcoded -- must match server's datagram len
    
    Socket tcpSocket;
    DatagramSocket udpSocket;
    
    PrintWriter writer = null;
    BufferedReader reader = null;

    try {
        Scanner sc = new Scanner(new FileReader(commandFile));
        
        udpSocket = new DatagramSocket();
        udpSocket.connect(new InetSocketAddress(hostAddress, udpPort));
        
        StringBuilder sb = new StringBuilder();

        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");
          
          if (tokens[0].equals("setmode")) {
        	  // TODO: set the mode of communication for sending commands to the server 
        	  
        	  if (tokens[1].equals("T") && type != 't') {
        		  System.out.println("Switch to TCP mode");
        		  tcpSocket = new Socket();
        		  tcpSocket.connect(new InetSocketAddress(hostAddress, tcpPort));
        		  writer = new PrintWriter(new OutputStreamWriter(tcpSocket.getOutputStream()));
        		  reader = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
        		  type = 't';
        	  }
        	  
        	  else if (tokens[1].equals("U") && type != 'u') {
        		  System.out.println("Switch to UDP mode");
        		  udpSocket = new DatagramSocket();
        		  udpSocket.connect(new InetSocketAddress(hostAddress, udpPort));
        		  type = 'u';
        	  }
        	  
        	  else {
        		  System.out.println("Socket type already " + ((type == 't') ? "TCP" : "UDP") + ".");
        	  }
        	  
          }
          else if (tokens[0].equals("exit")) {
        	  DatagramPacket datapacket;
    		  byte[] message = cmd.getBytes();
    		  datapacket = new DatagramPacket(message, message.length, udpSocket.getRemoteSocketAddress());
    		  udpSocket.send(datapacket);
    		  final FileWriter fw = new FileWriter(new File(String.format("out_%d.txt", clientId)));
    		  fw.write(sb.toString().trim());
    		  fw.close();
          }
          else {
        	  if (type == 't') {
        		  writer.print(cmd + "\r\n");
        		  writer.flush();
        		  char[] response = new char[1024];
        		  reader.read(response);
        		  String input = new String(response, 0, response.length);
        		  System.out.println(input.trim());
        		  sb.append(input.trim()).append("\n");
        	  } 
        	  else if (type == 'u') {
        		  DatagramPacket datapacket;
        		  byte[] message = cmd.getBytes();
        		  datapacket = new DatagramPacket(message, message.length, udpSocket.getRemoteSocketAddress());
        		  udpSocket.send(datapacket);
        		  byte[] response = new byte[1024];
        		  datapacket = new DatagramPacket(response, response.length);
        		  udpSocket.receive(datapacket);
        		  System.out.println(new String(datapacket.getData(), 0, datapacket.getLength()).trim());
        		  sb.append(new String(datapacket.getData(), 0, datapacket.getLength()).trim()).append("\n");
        	  } 
          }
        }
    } catch (FileNotFoundException e) {
    	e.printStackTrace();
    } catch (UnknownHostException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}