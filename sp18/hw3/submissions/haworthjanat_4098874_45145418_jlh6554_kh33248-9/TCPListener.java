import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPListener implements Runnable{
	int tcpPort;
	int udpPort;
	BookServer bs;
	
	public TCPListener(BookServer bs) {
		udpPort = bs.udpPort;
		tcpPort = bs.tcpPort;
		this.bs = bs;
	}

	@Override
	public void run() {
		try {
			ServerSocket listener = new ServerSocket(tcpPort);
			Socket s;
			
			while ( (s = listener.accept()) != null) {
				TCPServerThread TCPThread = new TCPServerThread(bs, s);
				Thread t = new Thread(TCPThread);
				t.start();
			}
			listener.close();

		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
