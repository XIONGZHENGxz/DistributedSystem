import java.io.*;
import java.net.*;

class NetworkConnector {
    String mode;

    DatagramSocket udpSocket;
    SocketAddress udpSendAddress;
    SocketAddress udpReceiveAddress;

    Socket tcpSocket;
    BufferedReader tcpInputStream;
    PrintWriter tcpOutputStreamWriter;

    static String byteArrayToString(byte[] data) {
        int i = 0;
        StringBuilder str = new StringBuilder();
        while (data[i] != 0) {
            str.append((char) data[i]);
            i++;
        }
        return str.toString();
    }

    NetworkConnector() {
        // no-arg constructor
    }

    void setUDPSendAddress(SocketAddress newAddress) {
        udpSendAddress = newAddress;
    }

    /**
     * Listens for a new message using UDP or TCP on the sockets given in the constructor.
     *
     * @return message that is received
     */
    String receive() throws IOException {
        System.out.println("Waiting for response...");
        String message =  mode.equals("U") ? receiveUDP() : receiveTCP();
        System.out.println("Received " + message + " via " + mode);
        return message;
    }

    private String receiveUDP() throws IOException {
        DatagramPacket rPacket = newDatagramPacket(256);
        udpSocket.receive(rPacket);
        return NetworkConnector.byteArrayToString(rPacket.getData());
    }

    private String receiveTCP() throws IOException {
        return tcpInputStream.readLine();
    }

    private DatagramPacket newDatagramPacket(int length) {
        byte[] buffer = new byte[length];
        return new DatagramPacket(buffer, buffer.length);
    }


    /**
     *  Sends a new message using UDP or TCP on the sockets given in the constructor.
     * @param message message to be sent
     */
    void send(String message) throws IOException {
        System.out.println("Sending " + message + " via " + mode);
        if (mode.equals("U")) {
            sendUDP(message);
        } else {
            sendTCP(message);
        }
        System.out.println("Sent " + message);
    }

    private void sendUDP(String message) throws IOException {
        byte[] buffer = message.getBytes();
        udpSocket.send(new DatagramPacket(buffer, buffer.length, udpSendAddress));
    }

    private void sendTCP(String message) throws IOException {
        tcpOutputStreamWriter.println(message);
    }

    void closeCurrentSocket() throws IOException {
        if (mode.equals("U")) {
            udpSocket.close();
        } else {
            tcpSocket.close();
            tcpInputStream.close();
            tcpOutputStreamWriter.close();
        }
    }
}
