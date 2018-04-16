import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCP_Communicator implements Communicator {
    Socket tcpSocket = null;  
    DataOutputStream os = null;
    BufferedReader is = null;
	int socket_number;
	
	TCP_Communicator(int socket_number){
		this.socket_number=socket_number;
	    try {
	        tcpSocket = new Socket("localhost", socket_number);
	        os = new DataOutputStream(tcpSocket.getOutputStream());
	        is = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
	    } catch (UnknownHostException e) {
	        System.err.println("Don't know about host: hostname");
	    } catch (IOException e) {
	        System.err.println("Couldn't get I/O for the connection to: hostname");
	    }
	}

	@Override
	public String send_message(String message) {
		String totalResponse = "";
		try {
			os.writeBytes(message+"\r");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(message);
		String response = "";
		String line;
		//String totalResponse = "";
		try {
			//while((line = is.readLine()) != null){
			//String totalResponse = "";
			response = is.readLine();
			//System.out.println(response);
			int counter = Integer.valueOf(response);
			while(counter != 0) {
				response = is.readLine();
				totalResponse += response+"\n";
				counter--;
			}
			//}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return totalResponse; 
		// TODO Auto-generated method stub
		
	}

	public void close() {
		try {
			os.writeBytes("exit");
			tcpSocket.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
