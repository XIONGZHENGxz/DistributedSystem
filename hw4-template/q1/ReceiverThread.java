import java.lang.Thread;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;

import java.io.InputStreamReader;

public class ReceiverThread extends Thread{
	BufferedReader in;
	String message;
	ServerSocket serverSocket;//listen for connection from peer servers
	ReceiverThread(int p){
		serverSocket=new ServerSocket(p);
	}

	public String getMessage(){
		return message;
	}

	public void run(){
		try{
			Socket socket=serverSocket.accept();
			in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String line="";
			while((line=in.readLine())!=null){
				message+=line;
			}
		} catch(IOException e){
			e.printStackTrace();
		}
	}
}



