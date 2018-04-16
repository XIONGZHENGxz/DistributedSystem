import java.util.Scanner;
import java.io.*;
import java.net.*;
import java.util.*;

public class BookClient {
  public static void main (String[] args) {
    String hostAddress;
    int tcpPort;
    int udpPort;
    int clientId;
    boolean mode = true; //true = UDP, false = TCP

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
    String filename = "out_" + clientId + ".txt";
    File file = new File(filename);

    try {
        Scanner sc = new Scanner(new FileReader(commandFile));
        InetAddress ia = InetAddress.getByName(hostAddress);
        BufferedWriter output = new BufferedWriter(new FileWriter(file));

        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");

          if (tokens[0].equals("setmode"))
          {
        	  mode = tokens[1].equals("U");
          }
          else if(mode)
          {
        	  DatagramSocket dataSocket = new DatagramSocket();
              byte[] returnBuffer = new byte[4096];
              byte[] sendBuffer = cmd.getBytes();
              DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, ia, udpPort);
              dataSocket.send(sendPacket);
              DatagramPacket recievePacket = new DatagramPacket(returnBuffer, returnBuffer.length);
              if(!tokens[0].equals("exit")) {
                  dataSocket.receive(recievePacket);
                  String retString = new String(recievePacket.getData(), 0, recievePacket.getLength());
                  output.write(retString);
              }
              else
              {
            	  output.close();
                  sc.close();
              }
              dataSocket.close();
          }
          else
          {
        	  Socket clientSocket = new Socket(hostAddress, tcpPort);
              char[] retBuffer = new char[4096];
              BufferedReader returnBuffer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
              PrintWriter sendBuffer = new PrintWriter(clientSocket.getOutputStream());
              sendBuffer.println(cmd);
              sendBuffer.flush();
              if(!tokens[0].equals("exit")) {
                  returnBuffer.read(retBuffer, 0, retBuffer.length);
                  output.write(new String(retBuffer));
              }
              else
              {
            	  output.close();
                  sc.close();
              }
              clientSocket.close();
          }
          
        }
    } catch (Exception e) {
	e.printStackTrace();
    }
  }
}
