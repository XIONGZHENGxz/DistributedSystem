import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UdpServer implements Runnable {
	
	Library library;
	DatagramSocket socket;
	int port, len;

	public UdpServer(int _port, Library _library) {
		port = _port;
		len = 1024;
		library = _library;
	}
	
	@Override
	public void run() {
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		System.out.println("UDP Server Socket running on " + port + ".");
		
		byte[] buf = new byte[len];
		DatagramPacket datapacket;
		
		try {
			while (true) {
				datapacket = new DatagramPacket(buf, len);
				socket.receive(datapacket);
				Thread thread = new Thread(new UdpController(datapacket, socket, library));
				thread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			socket.close();
		}
	}
}