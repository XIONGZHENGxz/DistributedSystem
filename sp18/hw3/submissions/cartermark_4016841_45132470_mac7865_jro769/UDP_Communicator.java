import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDP_Communicator implements Communicator {

	DatagramSocket udpSocket;
	InetAddress address; 
	int socket_number;
	UDP_Communicator(String host_name, int socket_number){
		this.socket_number = socket_number;
		try {
			udpSocket = new DatagramSocket();
			address = InetAddress.getByName(host_name);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@Override
	public String send_message(String message) {
//		try {
//			Thread.sleep(100);
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		byte[] buf = new byte[4096];
        buf = message.getBytes();
        DatagramPacket packet 
          = new DatagramPacket(buf, buf.length, address, socket_number);
        try {
			udpSocket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        buf = new byte[4096];
        packet = new DatagramPacket(buf, buf.length);
        try {
			udpSocket.receive(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String received = new String(
          packet.getData(), 0, packet.getLength());
        //System.out.println
        return received;
		// TODO Auto-generated method stub
		
	}
	
	public void close() {
		try {
			udpSocket.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
