import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by phucd on 3/5/2018.
 */
public class UDPListener extends Thread {
	Library library;
	int port;

	UDPListener(Library library, int port) {
		this.library = library;
		this.port = port;
	}

	public void run() {
		PrintStream pout;
		try {
			ServerSocket listener = new ServerSocket(port);
			while (true) {

				Socket s = listener.accept();
				pout = new PrintStream(s.getOutputStream());
				DatagramSocket ds = new DatagramSocket();
				System.out.println("local port: " + ds.getLocalPort());
				pout.println(ds.getLocalPort());
				Thread t = new UDPServerThread(ds, library);
				t.start();
				s.close();

				//	        pout = new PrintStream(s.getOutputStream());
				//	        pout.println(newPort);
				//	        Thread t = new ServerThread(library, newPort, false);
				//	        t.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}