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
			Server stub=(Server) registry.lookup("key/value store");
			if(msg.equals("ping")) return stub.Ping((int)args);
			else if(msg.equals("get")) return stub.Get((GetArg<String>)args);
			else if(msg.equals("putappend")) return stub.PutAppend((PutAppendArg<String>)args);
			else System.err.println("parameter error");
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public PingReply Ping(int viewNum){
		PingArg arg=new PingArg(viewNum,this.host);
		PingReply pr=(PingReply) this.Call("ping",arg);
		this.primary=pr.view.primary;
		return pr;
	}

	public GetReply Get(String key){
		GetArg<String> ga=new GetArg<String>(key);
		return (GetReply<String>)this.Call("get",ga);
	}
	
	public PutAppendReply PutAppend(String key,String value,String flag){
		PutAppendArg<String> paa=new PutAppendArg<String>(key,value,flag);
		return (PutAppendReply)this.Call("putappend",paa);
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

	public static void main(String...args){
		if(args.length<1) System.out.println("args...host,server,port ");
		Client client=new Client(args[0],args[1],Integer.parseInt(args[2]));
		client.Ping(0);
		System.out.println(client.primary);
		
	}

}




