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
		
	public Object Call(String msg,Object args){
		try{
			Registry registry=LocateRegistry.getRegistry(this.server,this.serverPort);
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

	public boolean Ping(){
		return (boolean) this.Call("ping",null);
	}

	public GetReply Get(String key){
		GetArg ga=new GetArg(key);
		return (GetReply<String>)this.Call("get",ga);
	}
	
	public PutAppendReply PutAppend(String key,String value,String flag){
		PutAppendArg<String> paa=new PutAppendArg<String>(key,value,flag);
		return (PutAppendReply)this.Call("putappend",paa);
	}

	//shutdown server
	public void shutdown(){
		this.Call("shutdown",null);
	}

	//resume server
	public void resume(){
		this.Call("resume",null);
	}

	public void put(String key,String value){
		this.PutAppend(key,value,"put");
	}

	public void Append(String key,String value){
		this.PutAppend(key,value,"append");
	}


	public String Primary(){
		return this.primary;
	}
}




