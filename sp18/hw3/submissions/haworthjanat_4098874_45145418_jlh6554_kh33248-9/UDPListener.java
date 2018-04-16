import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPListener implements Runnable{
	int tcpPort;
	int udpPort;
	BookServer bs;
	
	public UDPListener(BookServer bs) {
		udpPort = bs.udpPort;
		tcpPort = bs.tcpPort;
		this.bs = bs;
	}
	
	@Override
	public void run() {
		try {
			int len = 2048;
			DatagramPacket datapacket;
			DatagramSocket datasocket = new DatagramSocket(udpPort);
			byte[] buf = new byte[len];
			while (true) { 
				datapacket = new DatagramPacket(buf, buf.length);
				datasocket.receive(datapacket);
				
				String serverMessage = new String(datapacket.getData(), 0, datapacket.getLength());
				
				UDPServerThread st = new UDPServerThread(bs, serverMessage, datasocket, datapacket);
				Thread t = new Thread(st);
				t.start();

			}
		}
		catch (IOException e) {
			System.err.println(e);
		}
	}
}
