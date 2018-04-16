import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer implements Runnable {
		
	int port;
	Library library;
	ServerSocket socket;
	
	public TcpServer(int _port, Library _library) {
		port = _port;
		library = _library;
	}
	
	@Override
	public void run() {
		try {
			socket = new ServerSocket(port);
			System.out.println("TCP Server Socket running on " + port + ".");
			
			Socket s;
			while ((s = socket.accept()) != null) {
				System.out.println("new connection");
				Thread t = new Thread(new TcpController(s, library));
				t.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	} 
	
}
  