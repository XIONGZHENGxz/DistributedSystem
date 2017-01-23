package engine;

import compute.Task;
import compute.Compute;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;


public class ComputeEngine implements Compute{
	public ComputeEngine(){
		super();
	}

	public <T> T executeTask(Task<T> t){
		return t.execute();
	}


	public static void main(String...args){
		if(System.getSecurityManager()==null){
			System.setSecurityManager(new SecurityManager());
		}
		try{
			ComputeEngine engine=new ComputeEngine();
			Compute stub=(Compute) UnicastRemoteObject.exportObject(engine,0);
			Registry registry=LocateRegistry.getRegistry();
			registry.bind("compute",stub);
			System.out.println("compute engine is ready");
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}


