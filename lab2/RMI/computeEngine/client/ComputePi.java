
package client;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import compute.Compute;
import java.math.BigDecimal;

public class ComputePi{
	public static void main(String...args){
		if(System.getSecurityManager()==null){
			System.setSecurityManager(new SecurityManager());
		}

		try{
			Registry registry=LocateRegistry.getRegistry(args[0]);
			Compute stub=(Compute) registry.lookup("compute");
			Pi task=new Pi(Integer.parseInt(args[1]));
			BigDecimal pi=stub.executeTask(task);
			System.out.println("the result is..."+pi);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}

