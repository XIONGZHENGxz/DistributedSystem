import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
public class Client{
	String host;//client host:port
	String server;//primary server host
	int serverPort;//primary server port
	String primary;
	public Client(String name,String server,int port){
		this.host=name;
		this.server=server;
		this.serverPort=port;
		this.primary="";
	}
		
	public Object Call(String msg,Object args,String server,int port){
		try{
			Registry registry=LocateRegistry.getRegistry(server,port);
			ServerBase stub=(ServerBase) registry.lookup("key/value store");
			if(msg.equals("ping")) return stub.ClientPing();
			if(msg.equals("get")) return stub.Get((GetArg)args);
			else if(msg.equals("putappend")) return stub.PutAppend((PutAppendArg<String>)args);
			else if(msg.equals("shutdown")) stub.Shutdown();
			else if(msg.equals("resume")) stub.Resume();
			else System.err.println("parameter error");
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public boolean ping(String server,int port){
		return (boolean) this.Call("ping",null,server,port);
	}

	public GetReply get(String key){
		GetArg ga=new GetArg(key);
		return (GetReply<String>)this.Call("get",ga,this.server,this.serverPort);
	}
	
	public PutAppendReply PutAppend(String key,String value,String flag){
		PutAppendArg<String> paa=new PutAppendArg<String>(key,value,flag);
		return (PutAppendReply)this.Call("putappend",paa,this.server,this.serverPort);
	}

	//shutdown server
	public void shutdown(String server,int port){
		this.Call("shutdown",null,server,port);
	}

	//resume server
	public void resume(String server,int port){
		this.Call("resume",null,server,port);
	}

	public void put(String key,String value){
		this.PutAppend(key,value,"put");
	}

	public void append(String key,String value){
		this.PutAppend(key,value,"append");
	}


	public String Primary(){
		return this.primary;
	}
}




