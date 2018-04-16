/*
 * rsr873
 * vks377
 */

import java.io.IOException;
import java.net.*;

public class ServerUDPThread extends Thread {
    private int udpPort;
    private Library library;
    private DatagramSocket datasocket;
    private DatagramPacket datapacket;

    public ServerUDPThread(int port, Library l) {
        udpPort = port;
        library = l;
    }

    public void run() {
        try {
            datasocket = new DatagramSocket(udpPort);
            byte[] buf;
            while(true) {
                buf = new byte[65507];
                datapacket = new DatagramPacket(buf, buf.length);
                datasocket.receive(datapacket);
                UDPThread thread = new UDPThread(datasocket, datapacket, library);
                thread.start();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
