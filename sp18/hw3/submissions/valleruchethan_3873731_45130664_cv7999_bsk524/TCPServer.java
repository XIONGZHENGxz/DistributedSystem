import java.io.*;
import java.net.*;

public class TCPServer extends Thread {
	int port;
	ServerSocket listener;
	RecordLog log;
	BookInventory Inventory;

	
	public TCPServer(int port, RecordLog log, BookInventory Inventory) {
		this.port = port;
		this.log = log;
		this.Inventory = Inventory;
	}
	
	public void run() {

		try {
			listener = new ServerSocket(port);
			Socket s;
			while((s = listener.accept()) != null) {
				InputStream is = s.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String clientCommand = br.readLine();
				TCPWorker worker = new TCPWorker(s, clientCommand, log, Inventory);
				worker.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}