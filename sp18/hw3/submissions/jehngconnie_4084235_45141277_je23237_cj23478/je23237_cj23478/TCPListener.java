import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class TCPListener implements Runnable{
	ServerSocket tcpServerSocket = null;
    int tcpPort;
    String sentence = "";
    String response = "";
    boolean keepAlive = true;
    long threadId;
	private static final String EOM = "[EOM]";

    public TCPListener(int tcpPort){
    	this.tcpPort = tcpPort;
    	try {
			this.tcpServerSocket = new ServerSocket(tcpPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	public void shutDown(long curTid){
		try {
			keepAlive = false;
			// only close the socket for the non-current thread
			if(curTid != threadId)
				tcpServerSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// if there was a problem closing the socket, you would see it here.
			e.printStackTrace();
		}
    }
	@Override
	public void run() {
		threadId = Thread.currentThread().getId();
		//while(keepAlive){
			while(true){

			try{
			System.out.println("...waiting for next command");
			boolean keepReading = true;
			// print "listenening on TCP Socket X"
			Socket clientSocket = tcpServerSocket.accept();    			
	        //String clientAddress = clientSocket.getInetAddress().getHostAddress();
	        //System.out.println("\r\nNew connection from " + clientAddress);
	        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));//			while ( (data = in.readLine()) != null ) {
	        while ( keepReading && (sentence = in.readLine()) != null ) {
	        	System.out.println("Network '" + sentence + "'");
	        	if(EOM.equals(sentence.trim()))
	        		keepReading = false;
	        	else{
	        		response = BookServer.commandLineHandler(sentence);
	            //System.out.println("\r\nMessage from " + clientAddress + ": " + sentence);
	        		DataOutputStream sendClient = new DataOutputStream(clientSocket.getOutputStream());
	    		    //sentence = in.readLine();
	    		    //System.out.println("Received: " + sentence);
	    		    //String MySTRING = "TCP CONNECTION";
	    		    sendClient.writeBytes(response+"\n");
	    		    sendClient.writeBytes(EOM+"\n");
	    		    sendClient.flush();
	    		    //sendClient.close();
	        	}
	        	//clientSocket.close();
	        	//tcpServerSocket.close();
	        }
	        
			} catch(SocketException e){
				// if socket was closed internally, this will catch it.	
				//System.out.println("Internal Server Socket exception, "+e.getMessage()+".  Resetting");
				try {
					tcpServerSocket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//BookServer.stopAllListeningThreads();
	}


}
