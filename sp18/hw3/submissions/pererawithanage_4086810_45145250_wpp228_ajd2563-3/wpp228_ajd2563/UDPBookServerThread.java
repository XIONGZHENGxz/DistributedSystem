import java.net.*; 
import java.io.*; 
import java.util.*;

public class UDPBookServerThread extends Thread {

	private final int packetLen = 10000;

	private Inventory lib;
	private RecordList rec;
	private DatagramSocket ds;


	public UDPBookServerThread(Inventory lib, RecordList rec, DatagramSocket ds) {
		this.lib = lib;
		this.rec = rec;
		this.ds = ds;
	}

	public void run() {
		//System.out.println("[debug] UDP Client Handler spawned");
		try {
			String inStr, outStr;
			DatagramPacket in, out;

			while(true) {
				byte[] inBuf = new byte[packetLen];
				byte[] outBuf;
				in = new DatagramPacket(inBuf, inBuf.length);			
				ds.receive(in);

				inStr = new String(inBuf);
				//System.out.println("[debug] (" + inStr + ")");

				outStr = CommandParser.parseAndExecute(inStr, lib, rec);


				//System.out.println("[debug]\n" + outStr);
				outBuf = outStr.getBytes();

				//send out a return packet
				out = new DatagramPacket(outBuf, outBuf.length, in.getAddress(), in.getPort()); 
				ds.send(out);

				if (outStr.equals("End thread")) {
					ds.close();
					return;
				}
			}

		} catch (SocketException e) {
				System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}
	}
}
