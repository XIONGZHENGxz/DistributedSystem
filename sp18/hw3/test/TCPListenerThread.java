import java.net.*; 
import java.io.*; 
import java.util.*;

public class TCPListenerThread extends Thread {
	
	private final int tcpPort = 7000;
	private Inventory lib;
	private RecordList rec;

	public TCPListenerThread(Inventory lib, RecordList rec) {
		this.lib = lib;
		this.rec = rec;
	}

	public void run() {
		//System.out.println("Waiting for TCP connection on Port 7000");
		try {
			ServerSocket tcpListener = new ServerSocket(tcpPort);
			Socket s;

			while ((s = tcpListener.accept()) != null) {
				(new TCPBookServerThread(lib, rec, s)).start();
			}

		} catch (IOException e) {
			System.err.println(e);
		}
	}
}
