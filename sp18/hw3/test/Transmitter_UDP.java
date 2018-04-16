import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.io.IOException;
import java.net.UnknownHostException;

public class Transmitter_UDP implements Transmitter {
    InetAddress addr;
    DatagramSocket newUDP;
    int socketNum;

    Transmitter_UDP(int num, String host) {
        socketNum = num;
        try {
            newUDP = new DatagramSocket();
            addr = InetAddress.getByName(host);
        } catch(SocketException e) {
            System.err.println("Socket Exception in connection to localhost");
        } catch(IOException e) {
            System.err.println("I/O Exception in connection to localhost");
        } catch(UnknownHostException e) {
            System.err.println("Unknown host: localhost");
        }
    }

    @Override
    public String tranmsit_String(String str) {
        byte[] bytes = new byte[4096];
        bytes = str.getBytes();
        DatagramPacket newPack = new DatagramPacket(bytes, bytes.length, addr, socketNum);
        try {
            newUDP.send(newPack);
        } catch(IOException e) {
            System.err.println("I/O exception in string transmission");
        }
        bytes = new bytes[4096];
        newPack = new DatagramPacket(bytes, bytes.length);
        try {
            newUDP.receive(newPack);
        } catch (IOException e) {
            System.err.println("I/O Exception in string reception");
        }
        String incoming = new String(newPack.getData(), 0, newPack.getLength());
        return incoming;
    }

    public void close() {
        try {
            newUDP.close();
        } catch(IOException e) {
            System.err.println("I/O Exception in socket close");
        } catch(Exception e) {
            System.err.println("Exception in socket close");
        }
    }
}