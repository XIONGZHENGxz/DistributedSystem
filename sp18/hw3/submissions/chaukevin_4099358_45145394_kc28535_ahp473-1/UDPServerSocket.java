import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import static java.lang.Thread.yield;

public class UDPServerSocket implements Runnable {
    int connections;
    int port;
    BookServer bookServer;
    DatagramSocket welcomeSocket;
    public UDPServerSocket(BookServer bookServer) {
        this.bookServer = bookServer;
        welcomeSocket = null;
        try {
            welcomeSocket  = new DatagramSocket(7000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        port = 7000;
        byte[] buf = new byte[1024];
        byte[] send;
        boolean running  = true;

        while(running) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                welcomeSocket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("receieved");
            String received = new String(packet.getData(), 0, packet.getLength());
            System.out.println(received);
            System.out.println("now replying");
            port += 1;
            UDPThread t = new UDPThread(port, bookServer);
            Thread ts = new Thread(t);
            ts.start();
            sendPacket(packet, welcomeSocket);
        }
    }

    private  void sendPacket(DatagramPacket packet, DatagramSocket newSocket){
        byte[] toSend = (""+ port).getBytes();
        DatagramPacket p = new DatagramPacket(toSend,toSend.length, packet.getAddress(), packet.getPort());
        try {
            newSocket.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
