import javax.sound.midi.Soundbank;
import java.awt.print.Book;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class UDPThread implements Runnable {
    int port;
    BookServer bookServer;
    DatagramSocket datagramSocket;
    public UDPThread(int port, BookServer bookServer){
        System.out.println("Made a thread to listen on:" + port);
        this.port = port;
        this.bookServer = bookServer;
         datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }
    byte[] buf = new byte[1024];
    @Override
    public void run() {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        while (true) {
            try {
                System.out.println("waiting for a packet");
                datagramSocket.receive(packet);
                System.out.println("got a packet");
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println("got:" + received);
                String returnMessage = bookServer.messageHandler(received);
                System.out.println(returnMessage);
                if(returnMessage.equals("EXIT")) {
                    sendPacket(packet, datagramSocket, returnMessage);
                    datagramSocket.close();
                    return;
                }
                sendPacket(packet, datagramSocket, returnMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private  void sendPacket(DatagramPacket packet, DatagramSocket newSocket, String message){
        byte[] toSend = (message).getBytes();
        DatagramPacket p = new DatagramPacket(toSend,toSend.length, packet.getAddress(), packet.getPort());
        try {
            newSocket.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
