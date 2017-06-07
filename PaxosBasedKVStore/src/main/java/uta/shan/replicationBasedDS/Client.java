package uta.shan.ds;
import org.omg.CORBA.TIMEOUT;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;

public class Client{
	private static final int TIMEOUT = 3000;//3000 millis
	private String[] servers;
	private int[] ports;
	private Random rand;
	private String me;

	public Client(String name,String[] servers,int[] ports){
		this.me=name;
		this.servers=servers;
		this.ports=ports;
	}

	//get me
	public String getMe() {
		return this.me;
	}

	//get servers
	public String[] getServers() {
		return this.servers;
	}

	//generate unique id for every request
	public String makeRequestId(){
		int r=rand.nextInt(10000000);
		return me+String.valueOf(r);
	}

	//get
	public String get(String key){
		GetArg ga=new GetArg(key,makeRequestId(),me);
		GetReply gr=null;
		int i=0;
		for(;i<servers.length;i++){
			gr =(GetReply) sendWaitReply(ga,servers[i],ports[i]);
			if(gr!=null && gr.status==true) break;//if gr==null, then the server is down
		}
		if(i==this.servers.length) return "key does not exist!";
		return gr.value;
	}

	//send and wait for reply
	public Object sendWaitReply(Object request, String host, int port) {
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(host,port), TIMEOUT);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(request);
			out.flush();
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			Object reply = null;
			while(reply == null) {
				reply = in.readObject();
			}
			return reply;
		} catch (IOException e) {
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	//shared by put and append
	public boolean put(String key,String value){
		PutArg putArg=new PutArg(key,value,makeRequestId());
		PutReply reply = null;
		for(int i = 0;i<servers.length;i++) {
			reply = (PutReply) sendWaitReply(putArg,servers[i],ports[i]);
			if(reply != null && reply.getStatus()) return true;
		}
		return false;
	}

	/*
	//append to key
	public void append(String key,String value){
		for(int i=0;i<this.servers.length;i++){
			PutAppendReply par=this.PutAppend(key,value,this.servers[i],this.serverPorts[i],"append");
			if(par.status==true) break;
		}
	}
	*/

}




