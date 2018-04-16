import java.io.*;
import java.net.*;

class ClientNetworkConnector extends NetworkConnector {

    ClientNetworkConnector() {
        mode = "U";
        udpSocket = null;
        udpSendAddress = null;
        udpReceiveAddress = null;
    }

    ClientNetworkConnector(SocketAddress udpReceiveAddress, SocketAddress udpSendAddress) {
        try {
            mode = "U";
            this.udpReceiveAddress = udpReceiveAddress;
            this.udpSendAddress = udpSendAddress;
            udpSocket = new DatagramSocket(udpReceiveAddress);
        } catch (SocketException e) {
            udpSocket = null;
        }
    }

    void setMode(String mode, InetSocketAddress sendAddress) {
        try {
            if (!mode.equals(this.mode)) {
                switch (mode) {
                    case "U":
                        openUDPSocket();
                        break;
                    case "T":
                        establishTCPConnection(sendAddress);
                        break;
                    default:
                        throw new IllegalArgumentException(mode);
                }
                closeCurrentSocket();
                this.mode = mode;
            }
        } catch (IOException e) {
            System.err.println("ERROR: Client: setMode: " + e.getMessage());
        }
    }

    private void openUDPSocket() throws IOException {
        udpSocket = new DatagramSocket(udpReceiveAddress);
    }

    private void establishTCPConnection(InetSocketAddress sendAddress) throws IOException {
        System.out.println("Establishing TCP connection on port " + sendAddress.getAddress() + ", " + sendAddress.getPort());
        tcpSocket = new Socket(sendAddress.getAddress(), sendAddress.getPort());
        tcpInputStream = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
        tcpOutputStreamWriter = new PrintWriter(tcpSocket.getOutputStream(), true);
        System.out.println("Connection: " + tcpSocket.isConnected());
    }

//    void connect(InetAddress address, int port) throws IOException {
//        if (mode.equals("U")) {
//            udpSocket = new DatagramSocket(port, address);
//        } else {
//            tcpSocket = new Socket(address, port);
//            tcpInputStream = new BufferedInputStream(tcpSocket.getInputStream());
//            tcpOutputStreamWriter = new OutputStreamWriter(tcpSocket.getOutputStream());
//        }
//    }

}
