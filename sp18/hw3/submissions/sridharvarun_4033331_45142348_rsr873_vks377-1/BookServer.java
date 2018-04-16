/*
 * rsr873
 * vks377
 */

import java.io.*;
import java.net.*;

public class BookServer {
  public static void main (String[] args) throws IOException {
    int tcpPort;
    int udpPort;
    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    String fileName = args[0];
    tcpPort = 7000;
    udpPort = 8000;

    // parse the inventory file
    Library library = new Library(fileName);
    Thread udpServer = new ServerUDPThread(udpPort, library);
    udpServer.start();

    // TODO: handle request from clients
    try {
      ServerSocket listener = new ServerSocket(tcpPort);
      Socket s;
      while((s = listener.accept()) != null) {
        Thread t = new TCPThread(s, library);
        t.run();
      }
    } catch (IOException e) {
      System.err.println("Server aborted:" + e);
    }
  }
}
