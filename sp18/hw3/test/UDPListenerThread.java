import java.net.*; 
import java.io.*; 
import java.util.*;

public class UDPListenerThread extends Thread{
	
	private final int udpPort = 8000;
	private final int packetLen = 1024; 

	private Inventory lib;
	private RecordList rec;

	public UDPListenerThread(Inventory lib, RecordList rec) {
		this.lib = lib;	
		this.rec = rec;
	}

	public void run() {
		DatagramPacket inPacket, outPacket;

		//System.out.println("Waiting for UDP Connection on Port 8000");
		try {
			InetAddress addr = InetAddress.getByName("localhost");
			DatagramSocket udpListener = new DatagramSocket(udpPort, addr);
			byte[] inBuf = new byte[packetLen];
			byte[] outBuf;

			while (true) {
				inPacket = new DatagramPacket(inBuf, inBuf.length);
				udpListener.receive(inPacket);

				DatagramSocket s = new DatagramSocket();	

				outBuf = (s.getLocalPort() + "").getBytes();
				outPacket = new DatagramPacket(outBuf, outBuf.length, inPacket.getAddress(), inPacket.getPort());
				udpListener.send(outPacket);

				(new UDPBookServerThread(lib, rec, s)).start();
			}

		} catch (SocketException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}
	}	
}	
