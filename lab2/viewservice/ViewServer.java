import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
public class ViewServer implements ViewService{
	String host;
	int port;
	View view;
	public ViewServer(){}
	public ViewServer(String host,int port){
		this.port=port;
		this.host=host;
		try{
			ViewServer vs=new ViewServer();
			System.setProperty("java.rmi.server.hostname","192.168.245.146");
			ViewService stub=(ViewService) UnicastRemoteObject.exportObject(vs,0);
			Registry registry=LocateRegistry.createRegistry(this.port);
			registry.rebind("view service",stub);
			System.out.println("view service is runing");
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public PingReply Ping(PingArg args){
		int viewNum=args.viewNum;
		String hostPort=args.hostPort;
		if(view==null){
			view=new View(1,hostPort,"");
			System.out.println("assign primary to "+hostPort);
		}
		if(viewNum==0) System.out.println(hostPort+"crashed and restarted!");
		PingReply pr=new PingReply(this.view,false);
		return pr;
	}

	public View Get(){
		return view;	
	}

	public static void main(String...args){
		ViewServer vs=new ViewServer(args[0],Integer.parseInt(args[1]));
	}
}
