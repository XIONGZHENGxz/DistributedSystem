import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
public class Client{
	String host;//client host:port
	String server;//primary server host
	int serverPort;//primary server port
	public Client(String name,String server,int port){
		this.host=name;
		this.server=server;
		this.serverPort=port;
	}
		
	public Object Call(String msg,Object args){
		try{
			Registry registry=LocateRegistry.getRegistry(this.server,this.serverPort);
			ViewService stub=(ViewService) registry.lookup("view service");
			if(msg.equals("ping")) return stub.Ping((PingArg)args);
			else if(msg.equals("get")) return stub.Get();
			else System.err.println("parameter error");
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public PingReply Ping(int viewNum){
		PingArg arg=new PingArg(viewNum,this.host);
		return (PingReply)this.Call("ping",arg);
	}

	public View Get(){
		return (View)this.Call("get",null);
	}

	public String Primary(){
		View view=this.Get();
		return view.primary;
	}

	public static void main(String...args){
		Client client=new Client(args[0],args[1],Integer.parseInt(args[2]));
		client.Ping(0);
	}

}




