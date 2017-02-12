package uta.shan.ds;
import java.rmi.registry.Registry;
import java.util.concurrent.Semaphore;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.HashMap;
import java.lang.Runnable;
import uta.shan.paxos.Paxos;
import java.rmi.registry.LocateRegistry;

public class Server implements ServerBase,Runnable{
	String[] servers;
	int[] ports;
	int me;
	Paxos paxos;
	Map<String,String> store;
	Map<String,String> seen;
	Semaphore shut;
	public Server(int me,String[] servers,int[] ports){
		this.me=me;
		this.servers=servers;
		this.ports=ports;
		this.paxos=new Paxos(me,servers,ports);
		store=new HashMap<>();
		see=new HashMap<>();
		shut=new Semaphore(0);
	}

	//shut down itself, for test
	public void shutdown(){
		System.out.println("somebody wants me to shutdown!");
	}

	//resume
	public void resume(){
	}
	//read value
	public GetReply Get(GetArg arg){
		String key=arg.key;
		if(this.seen.get(arg.me).equals(arg.rid)) return new GetReply(this.preGets.get(arg.me));
		if(!store.containsKey(key)) return new GetReply();
		else{
			this.agree(arg);
			return new GetReply(store.get(key),true);
		}
	}
	
	//write value
	public PutAppendReply PutAppend(PutAppendArg arg) throws InvalidOperationException{
		String key=(String)arg.key;
		String val=(String)arg.value;
		if(arg.flag.equals("put"))
			store.put(key,val);
		else if(arg.flag.equals("append")) {
			if(store.containsKey(key)) store.put(key,store.get(key)+val);
			else store.put(key,val);
		}else throw new InvalidOperationException();
		PutAppendReply par=new PutAppendReply(true);
		return par;
	}

	public  void run(){
		try{
			System.setProperty("java.rmi.server.hostname",this.servers[this.me]);
			ServerBase stub=(ServerBase) UnicastRemoteObject.exportObject(this,0);
			Registry registry=LocateRegistry.createRegistry(this.ports[this.me]);
			registry.rebind("key/value store",stub);
			System.out.println("key value store ready!");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}

