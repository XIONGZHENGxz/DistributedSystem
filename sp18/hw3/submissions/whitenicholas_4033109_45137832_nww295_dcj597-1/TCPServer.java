//nww295
import java.io.*;
import java.net.*;
import java.util.*;

public class TCPServer extends Thread {
	private static int tcpPort = BookServer.tcpPort;
	
	public void run() {
		try {
			ServerSocket tcpSocket = new ServerSocket(tcpPort);
			Socket sock;
			while((sock = tcpSocket.accept()) != null) {
				TCPThread tcpThread = new TCPThread(sock);
				Thread runThread = new Thread(tcpThread);
				runThread.start();
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

}
