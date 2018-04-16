import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;

public class UDPListener implements Runnable{
	DatagramSocket serverSocket = null;
	DatagramPacket receivePacket, sendPacket;
    byte[] receiveData;
    byte[] sendData;
    String sentence = "";
    String response = "";
    boolean keepAlive = true;
    long threadId;
    int udpPort;
	public UDPListener(int udpPort) throws SocketException{
		this.udpPort = udpPort;
	}
	public void shutdown(long curTid){
		keepAlive = false;
		// only shutdown a thread if it is not current thread
		if(curTid != threadId)
			serverSocket.close();
	}
	@Override
	public void run() {
		//while(keepAlive) {
		while(true) {

			try {
			threadId = Thread.currentThread().getId();
			// print "listenening on UDP Socket X"
			receiveData = new byte[1024];
			serverSocket = new DatagramSocket(udpPort);
            receivePacket = new DatagramPacket(receiveData, receiveData.length);            
			serverSocket.receive(receivePacket);			
            sentence = new String(receivePacket.getData());
            System.out.println("RECEIVED: " + sentence);
            InetAddress IPAddress = receivePacket.getAddress();
            int returnPort = receivePacket.getPort();
            response = BookServer.commandLineHandler(sentence);
            sendData = new byte[1024];
            sendData = (response != null) ? response.getBytes() : new byte[0];
            sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, returnPort);
            System.out.println("Sending " + response);
			serverSocket.send(sendPacket);
			serverSocket.close();
			} catch (SocketException e){
				// if closed from the shutdown command, will catch upon exit
				System.out.println("Internal Server Socket exception, "+e.getMessage()+".  Resetting");

					serverSocket.close();

			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		//BookServer.stopAllListeningThreads();
	}
}
	

