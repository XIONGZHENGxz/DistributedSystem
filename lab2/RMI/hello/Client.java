import java.rmi.registry.Registry;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
public class Client{
	public static void main(String...args){
		String host=args.length<1?null:args[0];
		System.out.println(host);
		try{
			Hello stub=(Hello) Naming.lookup("rmi://"+host+"Hello");
			String response=stub.SayHello();
			System.out.println(response);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}

