/*
Mohamed Nasreldin man2766
Hamza Ghani hhg263
*/

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BookServer {
  public static void main (String[] args) {
    int tcpPort;
    int udpPort;

    String fileName = args[0];

    tcpPort = 7000;
    udpPort = 8000;

    parseInventory inventory = new parseInventory(fileName);

    try {
      Thread UDP;
      UDP = new UDPStart(inventory, udpPort);
      UDP.start();
    ServerSocket TCPServer = new ServerSocket(tcpPort);
    Socket sock;
      while ((sock = TCPServer.accept()) != null) {
        Thread t = new TCPHandle(inventory, sock);
        t.start();
      }
    } catch (IOException e) {}


  }
}
