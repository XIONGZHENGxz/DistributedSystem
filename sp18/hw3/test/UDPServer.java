//nww295
import java.io.IOException;
import java.util.*;
import java.net.*;

public class UDPServer extends Thread {
	private static int udpPort = BookServer.udpPort;
	private DatagramSocket udpSocket;
	private DatagramPacket udpPacket;
	
	public void run() {
		try {
			udpSocket = new DatagramSocket(udpPort);
			int packetLen = 65507;
			byte[] buffer;
			while(true) {
				try {
					buffer = new byte[packetLen];
					udpPacket = new DatagramPacket(buffer, packetLen);
					udpSocket.receive(udpPacket);
					UDPThread udpThread = new UDPThread(udpPacket, udpSocket);
					Thread udp = new Thread(udpThread);
					udp.start();
				} catch (IOException e) {
					System.err.println(e);
				}
			}
		} catch (SocketException e) {
			System.err.println(e);
		}
	}
}
