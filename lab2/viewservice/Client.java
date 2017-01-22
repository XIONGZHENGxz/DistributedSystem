import java.lang.ClassNotFoundException;
import java.net.Socket;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.ObjectInputStream;
import java.net.Socket;
public class Client{
	Socket socket;
	String name;//client host:port
	String server;//primary server host
	int serverPort;//primary server port
	BufferedWriter out;
	ObjectInputStream in;
	public Client(String name,String server,int port){
		this.name=name;
		this.server=server;
		this.serverPort=port;
	}
		
	public void Call(String msg,String srv,int port){
		try{
			this.socket=new Socket(srv,port);
			out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			in=new ObjectInputStream(socket.getInputStream());
			out.write(msg);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public PingReply Ping(int viewNum){
		PingArgs arg=new PingArgs(this.name,viewNum);
		this.Call("ping",this.server,this.serverPort);
		View reply=null;
		try{
			reply=(View) in.readObject();
		} catch(IOException | ClassNotFoundException e){
			e.printStackTrace();
		}
		if(reply==null) return new PingReply(reply,true);
		return new PingReply(reply,false);
	}

	public View get(){
		this.Call("getView",this.server,this.serverPort);
		View reply=null;
		try{
			reply=(View) in.readObject();
		} catch(IOException | ClassNotFoundException e){
			e.printStackTrace();
		}
		return reply;
	}

	public String Primary(){
		View view=this.get();
		return view.primary;
	}
}

class PingArgs{
	String hostPort;
	int viewNum;
	public PingArgs(String s,int v){
		hostPort=s;
		viewNum=v;
	}
}

class PingReply{
	View view;
	boolean error;
	public PingReply(View v,boolean b){
		view=v;
		error=b;
	}
}



