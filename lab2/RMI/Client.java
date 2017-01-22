import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
public class Client{
	public static void main(String...args){
		String host=args.length<1?null:args[0];
		try{
			Registry registry=LocateRegistry.getRegistry(host,1100);
			Hello stub=(Hello) registry.lookup("Hello");
			String response=stub.SayHello();
			System.out.println(response);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}

