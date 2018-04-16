//EID: pya74, brw922

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class BookClient {
  
  private static String[] parseMultipleOut(String in, boolean list) {
    String[] multipleStr = null;
    if(list) {
      multipleStr = in.split("\"  ");
    }else {
      multipleStr = in.split("  \"");
    }
    return multipleStr;
  }

  public static void main(String[] args) throws FileNotFoundException, IOException {
    int tcpPort;
    int udpPort;
    int clientID;
    boolean UDPMode = true;
    String hostAddress;
    String outputFile;
    boolean firstCommand = true;
    
    
    //UDP Variables
    InetAddress ia = null;
    DatagramSocket UDPSocket = null;
    boolean UDPSocClose = false;
    
    //TCP Variables
    PrintStream pout = null;
    Scanner din = null;
    Socket TCPSocket = null;
    boolean TCPSocClose = false;


    if (args.length != 2) {
      System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
      System.out.println("\t(1) <command-file>: file with commands to the server");
      System.out.println("\t(2) client id: an integer between 1..9");
      System.exit(-1);
    }
    
    // TODO Change to file
    //initialization of files
    //String commandFile = "Client1.txt";
    String commandFile = args[0];
    File file = new File(commandFile);
    hostAddress = "localhost";
    clientID = Integer.parseInt(args[1]);
    //clientID = 1;
    String[] multipleOut = null;
    
    //set up the ports 
    tcpPort = 7000;
    udpPort = 8000;
    
    //UDP Initialization - default communication 
    ia = InetAddress.getByName(hostAddress);
    UDPSocket = new DatagramSocket();
    UDPSocClose = true;
    
    //Initialize output file 
    outputFile = "out_"+clientID+".txt";
    FileWriter outputWrite = new FileWriter(outputFile);
    PrintWriter outputPrint = new PrintWriter(outputWrite);

    try(BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line;
      
      while((line = br.readLine()) != null) {
        //System.out.println(line);
        String tokens[] = line.split(" ");
        
        boolean listCommand = tokens[0].equals("list");
        boolean inventoryCommand = tokens[0].equals("inventory");
        //if command is setmode, change and set up the method of communication
        if (tokens[0].equals("setmode")) {
          if (tokens[1].equals("T")){
            UDPMode = false;
            //TCP Initialization 
            TCPSocket = new Socket("localhost", tcpPort);
            din = new Scanner(TCPSocket.getInputStream());;
            pout = new PrintStream(TCPSocket.getOutputStream());
            
            //close the socket for UDP
            TCPSocClose = true;
            if (UDPSocClose){
              UDPSocket.close();
              UDPSocClose = false;
            } 
          }else {
            pout.println(line);
            pout.flush();
            UDPMode = true;
            //UDP Initialization 
            ia = InetAddress.getByName(hostAddress);
            UDPSocket = new DatagramSocket();
            UDPSocClose = true;
            
            //close the socket for TCP
            if (TCPSocClose){
              TCPSocket.close();
              TCPSocClose = false;
            } 
          }
        }else {
          //UDP
          if (UDPMode){
            //sending to server
            byte[] buffer = new byte[line.length()];
            buffer = line.getBytes();
            DatagramPacket sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
            UDPSocket.send(sPacket);
            
            //receiving from server
            byte[] rbuffer = new byte[1024];
            DatagramPacket rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            UDPSocket.receive(rPacket);
            String retString = new String(rPacket.getData(), 0, rPacket.getLength());
            retString = retString.trim(); //remove any white spaces
  
            //if need to print out multiple lines, parse string to print on different lines
            if (listCommand || inventoryCommand) {
              multipleOut = parseMultipleOut(retString, listCommand);
              for(int i = 0; i<multipleOut.length; i++) {
                String outStr = "";
                if(inventoryCommand && i != 0) {
                  outStr += "\"";
                }
                outStr += multipleOut[i];
                if(listCommand && i != multipleOut.length - 1) {
                  outStr += "\"";
                  
                }
                
                if(firstCommand){
                  firstCommand = false;
                } else {
                  outputPrint.print("\n");
                }
                //outputPrint.println(outStr.trim());
                outputPrint.print(outStr.trim());
                //outputPrint.print(outStr.trim() + "\r\n");
                
              }
              listCommand = false;
              inventoryCommand = false;
            }else {
              if(firstCommand){
                firstCommand = false;
              } else {
                outputPrint.print("\n");
              }
              //outputPrint.println(retString.trim());
              outputPrint.print(retString.trim());
              //outputPrint.print(retString.trim() + "\r\n");
            }
            
          }else { 
            //TCP
            String input;
            pout.println(line);
            pout.flush();
            
          //close streams if exiting
            if (tokens[0].equals("exit")) {
              pout.close();
            } else if (listCommand || inventoryCommand) {
              input = din.nextLine();
              input = input.trim();
              
              multipleOut = parseMultipleOut(input, listCommand);
              for(int i = 0; i<multipleOut.length; i++) {
                String outStr = "";
                if (inventoryCommand && i != 0) {
                  outStr += "\"";
                }
                outStr += multipleOut[i];
                if (listCommand && i != multipleOut.length - 1) {
                  outStr += "\"";
                }
                
                if(firstCommand){
                  firstCommand = false;
                } else {
                  outputPrint.print("\n");
                }
                //outputPrint.println(outStr.trim());
                outputPrint.print(outStr.trim());
                //outputPrint.print(outStr.trim() + "\r\n");
              }
              listCommand = false;
              inventoryCommand = false;
            }else {
              //prints out response from server
              input = din.nextLine();
              input = input.trim();
              if(firstCommand){
                firstCommand = false;
              } else {
                outputPrint.print("\n");
              }
              //outputPrint.println(input.trim());
              outputPrint.print(input.trim());
              //outputPrint.print(input.trim() + "\r\n");
            }
            
            
          }
        }        
      }
      outputPrint.close();
      if (UDPSocClose){
        UDPSocket.close();
        UDPSocClose = false;
      } 
      if (TCPSocClose){
        TCPSocket.close();
        TCPSocClose = false;
      } 
    }
  }
}
