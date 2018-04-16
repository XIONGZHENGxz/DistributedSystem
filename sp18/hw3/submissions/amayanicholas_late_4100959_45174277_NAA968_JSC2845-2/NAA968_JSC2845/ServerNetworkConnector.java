import java.io.*;
import java.net.*;

class ServerNetworkConnector extends NetworkConnector {

    private ConcurrentSocketFactory socketFactory;

    ServerNetworkConnector() {
        // no-arg constructor
    }

    ServerNetworkConnector(
            ConcurrentSocketFactory socketFactory,
            DatagramSocket udpSocket,
            SocketAddress udpSendAddress)
    {
        this.mode = "U";
        this.udpSocket = udpSocket;
        this.udpSendAddress = udpSendAddress;
        this.socketFactory = socketFactory;
        this.tcpSocket = null;
        this.tcpInputStream = null;
        this.tcpOutputStreamWriter = null;
    }

    /**
     * Sets protocol of network connector. Either U or T for UDP and TCP, respectively.
     * @param mode mode. Either "U" or "T"
     */
    void setMode(String mode) {
        try {
            if (!mode.equals(this.mode)) {
                System.out.println("Setting mode to " + mode + "...");
                switch (mode) {
                    case "U":
                        openUDPSocket();
                        break;
                    case "T":
                        establishTCPConnection();
//                        System.out.println("TCP Connection established on " + tcpSocket.getLocalPort());
                        break;
                    default:
                        throw new IllegalArgumentException(mode);
                }
                closeCurrentSocket();
                this.mode = mode;
            }
        } catch (IOException e) {
            System.err.println("ERROR: setMode: IOException: " + e.getMessage());
        }
    }

    private void openUDPSocket() throws IOException {
        udpSocket = socketFactory.newUDPSocket();
    }

    private void establishTCPConnection() throws IOException {
        System.out.println("Establishing TCP connection");
        Socket newSocket = socketFactory.newTCPSocket(this);
        tcpSocket = newSocket;
        tcpInputStream = new BufferedReader(new InputStreamReader(newSocket.getInputStream()));
        tcpOutputStreamWriter = new PrintWriter(newSocket.getOutputStream(), true);
    }

}
