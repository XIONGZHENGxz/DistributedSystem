import java.net.*;
import java.io.*;
import java.util.*;

public class UDPThread implements Runnable {
    private DatagramSocket datagramSocket;
    private DatagramPacket datagramPacket;

    public UDPThread(DatagramPacket packet, DatagramSocket socket) {
        datagramPacket = packet;
        datagramSocket = socket;
    }

    public void run() {
	    String message = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
	    InetAddress send = datagramPacket.getAddress();
	    int clientPort = datagramPacket.getPort();
	    String[] initArgs = new String[2];
	    initArgs = message.split(" ");
	    if(initArgs[0].equals("exit")) {
	        String reply = "exit";
	        byte[] replyByte = reply.getBytes();
	        DatagramPacket exit = new DatagramPacket(replyByte, replyByte.length, send, clientPort);
	        try {
	            datagramSocket.send(exit);
	            Library.printInventory();
            } catch(IOException e) {
	            e.printStackTrace();
            }
            return;
        }
	    String reply = MultithreadedServer.processResponse(message);
	    byte[] replyBytes = reply.getBytes();
	    DatagramPacket replyPacket = new DatagramPacket(replyBytes, replyBytes.length, send, clientPort);
	    try {
		    datagramSocket.send(replyPacket);
	    } catch(IOException e) {
		    e.printStackTrace();
	    }

    }

}
