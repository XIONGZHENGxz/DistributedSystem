package uta.shan.ds;
import java.rmi.registry.Registry;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.HashMap;
import java.lang.Runnable;
import uta.shan.paxos.*;
import java.rmi.registry.LocateRegistry;

public class Server implements ServerBase,Runnable{
	String[] servers;
	int[] ports;
	public int me;
	int seed;
	ReentrantLock lock;
	Paxos paxos;
	Map<String,String> store;
	Map<String,String> seen;
	Map<String,String> prevGets;	
	Random rand;
	Semaphore shut;
	public Server(int me,String[] servers,int[] ports){
		this.rand=new Random();
		this.seed=0;
		this.me=me;
		this.servers=servers;
		this.ports=ports;
		this.paxos=new Paxos(me,servers,ports);
		store=new HashMap<>();
		seen=new HashMap<>();
		prevGets=new HashMap<>();
		shut=new Semaphore(0);
		lock=new ReentrantLock();
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
		if(this.seen.get(arg.me).equals(arg.rid)) return new GetReply(this.prevGets.get(arg.me),true);
		if(!store.containsKey(key)) return new GetReply();
		else{
			this.agree(new Operation(arg.rid,arg.key,null,"get",arg.me));
			this.seen.put(arg.me,arg.rid);
			return new GetReply(store.get(key),true);
		}
	}
	//wait for paxos to reach agreement
	public Operation reachAgreement(int seq){
		while(true){
			Status s=this.paxos.getStatus(seq);
			if(s==Status.DECIDED){
				return (Operation)this.paxos.getOperation(seq);
			}else if(s==Status.PENDING){
				try{
					Thread.sleep(10);
				} catch(InterruptedException e){
					e.printStackTrace();
				}
			}
		}
	}

	//commit operation
	public void commitOperation(int seq,Operation op){
		if(op.type.equals("get")){
			String prev="";
			if(this.store.containsKey(op.key)) prev=this.store.get(op.key);
			this.prevGets.put(op.key,prev);
		}else if(op.type.equals("put")){
			this.store.put(op.key,op.value);
		}else{
			this.store.put(op.key,this.store.get(op.key)+op.value);
		}
		this.paxos.Done(seq);
	}

	//agree on an operation
	public void agree(Operation op){
		this.seed++;
		Status status=this.paxos.getStatus(this.seed);
		if(status==Status.PENDING){
			this.paxos.start(this.seed,op);
			this.commitOperation(this.seed,op);
		}else if(status==Status.DECIDED)
			this.commitOperation(this.seed,op);
	}
		
	//write value
	public PutAppendReply PutAppend(PutAppendArg arg){
		lock.lock();
		if(this.seen.get(arg.me)==arg.rid) return null;
		String key=arg.key;
		String val=arg.value;
		this.agree(new Operation(arg.key,arg.value,arg.flag,arg.rid,arg.me));
		this.seen.put(arg.me,arg.rid);
		lock.unlock();
		return new PutAppendReply(true);
	}
	@Override
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

