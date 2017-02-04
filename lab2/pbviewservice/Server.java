import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.HashMap;
import java.rmi.registry.LocateRegistry;

public class Server implements ServerBase{
	String host;//server host:port
	int port;
	String viewServer;//view service server host
	int viewServerPort;//view service server port
	int viewNum;
	long lastPingTime;
	Map<String,String> store;
	boolean isPrimary;
	View view;
	public Server(String name,int myPort,String server,int port){
		this.host=name;
		this.port=myPort;
		this.viewServer=server;
		this.viewServerPort=port;
		this.viewNum=0;
		lastPingTime=System.currentTimeMillis();
		isPrimary=false;
		store=new HashMap<>();
	}

	public Object Call(Object arg){
		PingReply pr=null;
		try{
			Registry registry=LocateRegistry.getRegistry(this.viewServer,this.viewServerPort);
			ViewService stub=(ViewService) registry.lookup("view service");
			pr=stub.Ping((PingArg)arg);
		} catch(Exception e){
			e.printStackTrace();
		}
		return pr;
	}

	public void Copy(Map<String,String> map){
		try{
			Registry registry=LocateRegistry.getRegistry(this.view.backup,this.port);
			ServerBase stub=(ServerBase) registry.lookup("key/value store");
			stub.CopyStore(map);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public void CopyStore(Map<String,String> map){
		this.store=map;
	}

	//ping view service periodically 
	public PingReply Ping(int viewNum){
		PingArg arg=new PingArg(viewNum,this.host);
		PingReply pr=(PingReply) this.Call(arg);
		if(this.isPrimary && !pr.view.backup.equals(this.view.backup)){
			this.view=pr.view;
			this.Copy(this.store);
		}
		return pr;
	}
	
	//read value
	public GetReply Get(GetArg arg) throws NotFoundException{
		String key=(String) arg.key;
		if(!store.containsKey(key)) throw  new NotFoundException(key);
		else{
			GetReply<String> gr=new GetReply<String>(store.get(key));
				return gr;
		}
	}
	
	//write value
	public PutAppendReply PutAppend(PutAppendArg arg){
		String key=(String)arg.key;
		String val=(String)arg.value;
		store.put(key,val);
		PutAppendReply par=new PutAppendReply(true);
		return par;
	}

	//ping the view service periodically 
	public void tick(){
		PingReply pr=this.Ping(this.viewNum++);
		this.view=pr.view;
		if(pr.view.primary.equals(this.host)) this.isPrimary=true;
		else this.isPrimary=false;
		System.out.println(this.view.primary);
	}	
		
	public String Primary(){
		PingReply pr=this.Ping(this.viewNum);
		return pr.view.primary;
	}

	public static void main(String...args){
		if(args.length<1){
			System.err.println("args...host,port,view server host,view server port");
			return;
		}
		Server server=new Server(args[0],Integer.parseInt(args[1]),args[2],Integer.parseInt(args[3]));
		try{
			System.setProperty("java.rmi.server.hostname",args[0]);
			ServerBase stub=(ServerBase) UnicastRemoteObject.exportObject(server,0);
			Registry registry=LocateRegistry.createRegistry(server.port);
			registry.rebind("key/value store",stub);
			System.out.println("key value store ready!");
		}catch(Exception e){
			e.printStackTrace();
		}
		PrimaryMonitor pm=new PrimaryMonitor(server);
		pm.start();
	}
}


class PrimaryMonitor extends Thread{
	Server server;
	public PrimaryMonitor(Server s){
		this.server=s;
	}

	@Override 
	public void run(){
		while(true){
			long currentTime=System.currentTimeMillis();
			if(currentTime-server.lastPingTime>=Common.PingInterval){
				server.tick();
			}
		}
	}
}
