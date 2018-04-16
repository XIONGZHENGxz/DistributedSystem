
import java.io.IOException;
import java.net.*;

public class Listener implements Runnable {
	
	ServerSocket serversocket;
	
	Library library;
	
	Listener (Library library) {
		this.library = library;
	}

	@Override
	public void run() {
		try { 
			serversocket = new ServerSocket(7000); 
			while(true) {
				Socket socket = serversocket.accept();
				// listening and create a new librarian for a new client
				Thread librarian = new Thread( new Librarian(library, socket) );
	
				librarian.start();
			}
		} 
		catch (IOException e) { e.printStackTrace(); }
		

	}

}
