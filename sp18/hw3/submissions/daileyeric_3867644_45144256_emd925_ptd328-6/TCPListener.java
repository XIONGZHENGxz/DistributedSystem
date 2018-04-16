import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by phucd on 3/5/2018.
 */
public class TCPListener extends Thread {
	Library library;
	int port;
	int thread_count;

	public TCPListener(Library l, int p) {
		this.library = l;
		this.port = p;
		thread_count = 1;
	}

	public void run() {
		PrintStream pout;
		try {
			ServerSocket listener = new ServerSocket(port);
			while (true) {
				Socket s = listener.accept();
				pout = new PrintStream(s.getOutputStream());
				ServerSocket ss = new ServerSocket(0);
				thread_count++;
				pout.println(ss.getLocalPort());
				Thread t = new TCPServerThread(ss, library);
				t.start();
				s.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
