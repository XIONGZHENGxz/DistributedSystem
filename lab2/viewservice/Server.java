import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
public class Server{
	String host;//server host:port
	String viewServer;//view service server host
	int viewServerPort;//view service server port
	int viewNum;
	long lastPingTime;
	public Server(String name,String server,int port){
		this.host=name;
		this.viewServer=server;
		this.viewServerPort=port;
		this.viewNum=0;
		lastPingTime=System.currentTimeMillis();
	}
		
	public Object Call(String msg,Object args){
		try{
			Registry registry=LocateRegistry.getRegistry(this.viewServer,this.viewServerPort);
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
		if(args.length<1){
			System.err.println("args...host,view server host,view server port");
			return;
		}
		Server server=new Server(args[0],args[1],Integer.parseInt(args[2]));
		while(true){
			long currentTime=System.currentTimeMillis();
			if(currentTime-server.lastPingTime>=Common.PingInterval){
				server.lastPingTime=currentTime;
				server.Ping(server.viewNum++);
				System.out.println(server.Primary());
			}
		}
	}

}




