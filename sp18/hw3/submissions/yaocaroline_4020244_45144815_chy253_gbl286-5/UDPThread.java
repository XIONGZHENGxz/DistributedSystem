//package hwk3;

import java.io.IOException;
import java.net.*;

public class UDPThread extends Thread {

    int port;
    final int len = 1024;

    public UDPThread(int udpPort) {
        port = udpPort;
    }

    public void run(){
        DatagramSocket listener;
        try {
            listener = new DatagramSocket(port);
            byte[] initBuff = new byte[len];

            // listen for new clients
            while(true) {
                // wait for udp request from client
                DatagramPacket dataPacket = new DatagramPacket(initBuff, initBuff.length);
                listener.receive(dataPacket);

                // create new socket and thread to handle request
                DatagramSocket uniqueSocket = new DatagramSocket();
                Thread worker = new UDPClientHandler(uniqueSocket, uniqueSocket.getLocalPort());
                worker.start();

                // send client the thread's unique port
                byte[] buf = String.valueOf(uniqueSocket.getLocalPort()).getBytes();
                DatagramPacket portPacket = new DatagramPacket(buf, buf.length, dataPacket.getAddress(), dataPacket.getPort());
                listener.send(portPacket);
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
