import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
public class ViewServer implements ViewService{
	String host;
	int port;
	public ViewServer(){}
	public ViewServer(String host,int port){
		this.port=port;
		this.host=host;
		try{
			ViewServer vs=new ViewServer();
			ViewService stub=(ViewService) UnicastRemoteObject.exportObject(vs,0);
			Registry registry=LocateRegistry.createRegistry(1099);
			registry.bind("view service",stub);
			System.out.println("view service is runing");
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public void Ping(PingArgs args,PingReply reply){
	}

	public void Get(GetArgs args,GetReply reply){
	}
		
}
