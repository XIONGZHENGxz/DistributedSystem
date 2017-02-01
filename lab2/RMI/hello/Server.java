import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;
public class Server implements Hello{
	public String SayHello(){
		return "Hello,World";
	}
	public static void main(String...args){
		try{
			Server s=new Server();
			System.setProperty("java.rmi.server.hostname","192.168.245.149");
			Hello stub=(Hello) UnicastRemoteObject.exportObject(s,54100);
			Registry registry=LocateRegistry.getRegistry();
			registry.rebind("Hello",stub);
			System.out.println("server ready");
		} catch(Exception e){
			e.printStackTrace();
		}

	}
}
