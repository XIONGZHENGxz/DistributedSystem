import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class BookServer {

  public static void main (String[] args) {
    int tcpPort;
    int udpPort;
    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial library");
      System.exit(-1);
    }
    String fileName = args[0];
    tcpPort = 7000;
    udpPort = 8000;
    // parse the library file
    Library library = new Library(fileName);

    TCPListener tListener = new TCPListener(library, tcpPort);
    UDPListener uListener = new UDPListener(library, udpPort);
    tListener.start();
    uListener.start();

    // TODO: handle request from clients
  }

}

