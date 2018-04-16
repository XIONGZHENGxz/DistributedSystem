//package homework3;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class TCPServer extends Thread {

	int tcpPort;
	//NameTable table; 
	
	public TCPServer(int tcpPort) {
		this.tcpPort = tcpPort;
		//table = new Nametable();
	}

	public void run() {
		try {
			ServerSocket listener = new ServerSocket(tcpPort);
			Socket s;
			int count = 0;
			while ((s = listener.accept()) != null) {
				System.out.println("count of TCPServer: "+count);
				Thread t = new TCP_thread(s);
				t.start();
				count++;

			}
			listener.close();

		} catch (IOException e) {
			System.err.println(e);
		}
	}
}