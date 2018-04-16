import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;

public abstract class ServerThread implements Runnable {
    public class Packet {
        DatagramPacket incoming;
        DatagramPacket send;
        InputStream incomingStream;
        OutputStream outputStream;

        public Packet(DatagramPacket datagramPacket) {

        }

        public Packet(InputStream incomingStream) {

        }
    }

    public void listen(Packet p) {

    }

    public void send(Packet p) {

    }
}
