//EID: pya74, brw922

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;

public class BookServer {
  public static void main(String[] args) {
    int TCPPort = 7000;
    int UDPPort = 8000;
    
    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    
    ArrayList<String> titles = new ArrayList<String>();
    ArrayList<Integer> count = new ArrayList<Integer>();
    // TODO Change to args[0]
    //String fileName = "Input.txt";
    String fileName = args[0];
    
    
    //parse inventory file
    try {
      FileInputStream file = new FileInputStream(fileName);
      InputStreamReader inputStream = new InputStreamReader(file);
      BufferedReader br = new BufferedReader(inputStream);

      String line;
      while ((line = br.readLine()) != null) {
         int start = line.indexOf("\" ");
         titles.add(line.substring(0, start+1));
         count.add(Integer.valueOf(line.substring(start+2)));
      }
      
      br.close();
    }catch(FileNotFoundException e){
      System.err.println(e);
    }catch(IOException e){
      System.err.println(e);
    }

    BookInventory inventory = new BookInventory(titles, count);
   
    
    //start up TCP and UDP threads to listen to client's request
    try {
      DatagramSocket udpSocket = new DatagramSocket(UDPPort);
      ServerSocket tcpSocket = new ServerSocket(TCPPort);
      TCPServer TCP = new TCPServer(tcpSocket, inventory);
      UDPServer UDP = new UDPServer(udpSocket, inventory);
      Thread t1 = new Thread(TCP);
      Thread t2 = new Thread(UDP);
      t1.start();
      t2.start();
     
    } catch (SocketException e) {
      System.err.println(e);
    } catch (IOException e) {
      System.err.println(e);
    }
  }
}

//waits for TCP messages
class TCPServer implements Runnable {
  ServerSocket socket;
  BookInventory inventory;
  
  public TCPServer(ServerSocket socket, BookInventory inventory) {
    this.socket = socket;
    this.inventory = inventory;
  }
  
  @Override
  public void run() {
    //TODO: is there a need to find a way to stop bookServer class?
    while(true) {
      try {
        Socket clientSocket = socket.accept();
        TCPThread TCPClient = new TCPThread(clientSocket, inventory);
        Thread t1 = new Thread(TCPClient);
        t1.start();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
  }
}

//waits for UDP messages
class UDPServer implements Runnable {
  DatagramSocket socket;
  BookInventory inventory;
  
  public UDPServer(DatagramSocket socket, BookInventory inventory) {
    this.socket = socket;
    this.inventory = inventory;
  }
  
  @Override
  public void run() {
    
    while (true) {
      byte[] buf = new byte[1024]; // TODO: Populate buf with messages to send back to client
      DatagramPacket dataPacket = new DatagramPacket(buf, buf.length);  
      
      try {
        socket.receive(dataPacket);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      UDPThread UDPPacket = new UDPThread(dataPacket, socket, inventory);
      Thread t1 = new Thread(UDPPacket);
      t1.start();
    }
    
  }
  
}