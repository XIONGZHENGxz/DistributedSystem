import java.io.IOException;
import java.net.*;

public class UDPServer extends Thread {
	private int port;
	private DatagramSocket s;
	private DatagramPacket p;
	RecordLog log;
	BookInventory Inventory;

	
	public UDPServer(int port, RecordLog log, BookInventory Inventory) {
		this.port = port;
		this.port = port;
		this.log = log;
		this.Inventory = Inventory;
	}
	
	public void run() {
		byte[] buffer = new byte[1024];
		
		try {
			s = new DatagramSocket(port);
	
			while(true) {	
				p = new DatagramPacket(buffer, buffer.length);
				s.receive(p);
				UDPWorker worker = new UDPWorker(s, p, log, Inventory);
				worker.start();
			}
			
		} catch (IOException e) {
				e.printStackTrace();
		}
	}
}