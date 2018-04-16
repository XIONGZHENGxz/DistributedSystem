import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPThread extends Thread {
    DatagramPacket sendPacket, receivePacket;
    DatagramSocket udpSocket;
    int udpPort;
    private static final int maxLength = 1024;
    byte[] buffer;

    public UDPThread(int udpPort){
        this.udpPort = udpPort;
        buffer = new byte[maxLength];
        try {
            udpSocket = new DatagramSocket(udpPort);
        } catch (SocketException e){
            System.out.println(e);
        }

    }

    public void run(){
        while(true){
            receivePacket = new DatagramPacket(buffer, buffer.length);
            try {
                udpSocket.receive(receivePacket);
                //System.out.println("Received UDP Packet");
                // Make a new thread to handle this client connection
                ServerClientThread udpClientThread = new ServerClientThread(udpSocket, receivePacket);
                udpClientThread.start();

//                sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), receivePacket.getPort());
//                udpSocket.send(sendPacket);
            } catch (IOException e){
                e.printStackTrace();
                break;
            }
        }
        udpSocket.close();
    }
}
