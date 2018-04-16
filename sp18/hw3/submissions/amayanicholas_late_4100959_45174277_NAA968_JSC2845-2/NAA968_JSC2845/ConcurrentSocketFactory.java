import java.io.IOException;
import java.net.*;
import java.util.concurrent.Semaphore;

public class ConcurrentSocketFactory {

    private int udpPort;
    private Semaphore serverSocketGate;
    private ServerSocket tcpServerSocket;

    ConcurrentSocketFactory(int udpPort, int tcpPort) {
        try {
            serverSocketGate = new Semaphore(1);
            tcpServerSocket = new ServerSocket(tcpPort);
            this.udpPort = udpPort;
        } catch (IOException e) {
            tcpServerSocket = null;
        }
    }

    DatagramSocket newUDPSocket() throws SocketException {
        udpPort += 1;
        SocketAddress address = new InetSocketAddress("localhost", udpPort);
        return new DatagramSocket(address);
    }

    Socket newTCPSocket(NetworkConnector network) throws IOException {
        try {
            serverSocketGate.acquire();
            network.send("tcp setup complete");
            Socket newClientSocket = tcpServerSocket.accept();
            serverSocketGate.release();
            return newClientSocket;
        } catch (InterruptedException | NullPointerException e) {
            System.err.println("ERROR: ConcurrentSocketFactory: " + e.getMessage());
            return null;
        }
    }

}
