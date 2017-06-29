import java.lang.Thread;
import java.net.InetSocketAddress;

public class HandlerThread extends Thread{
	String request;
	Server server;//my server
	InetSocketAddress addr;//socket address I am connecting to
	HandlerThread(String req,Server s,InetSocketAddress addr){
		request=req;
		server=s;
		this.addr=addr;
	}

	public void run(){
		System.out.println("handler started....");
		server.requestCS();
		System.out.println("critical section requested...");
		server.handler(request,addr);
		server.releaseCS();
	}
}
