package uta.shan.ds.server;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.lang.Runnable;

import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import uta.shan.ds.paxos.Paxos;
import uta.shan.ds.common.PutAppendArg;
import uta.shan.ds.common.GetReply;
import uta.shan.ds.common.Type;
import uta.shan.ds.common.GetArg;
import uta.shan.ds.common.PutAppendReply;
import uta.shan.ds.common.Operation;
import uta.shan.ds.client.ClientOp;
import uta.shan.ds.paxos.Status;


public class Server implements ServerBase,Runnable{
	List<String> servers;
	String[] masters;
	List<Integer> ports;
	Configuration config;
	List<Integer> mports;
	public int me;
	int seed;
	ReentrantLock lock;
	Paxos paxos;
	Map<String,String> store;
	Map<String,String> seen;
	Map<String,String> prevGets;	
	Random rand;
	Semaphore shut;
	public Server(int me,String[] masters,int[] masterports){
		this.rand=new Random();
		this.seed=0;
		this.me=me;
		this.masters=masters;
		this.mports=masterports;
		this.paxos=new Paxos(me,servers,ports);
		store=new HashMap<>();
		seen=new HashMap<>();
		prevGets=new HashMap<>();
		shut=new Semaphore(0);
		lock=new ReentrantLock();
	}
	
	//RPC call
	public Object call(Type type,Object arg){
		Registry registry=null;
		for(int i=0;i<this.masters.length;i++){
			try{
				registry=LocateRegistry.getRegistry(this.masters[i],this.mports[i]);
				stub=registry.lookup("shardedmaster");
				if(type==Type.JOIN) stub.join((JoinArg)arg);
				else if(type==Type.LEAVE) stub.leave((LeaveArg)arg);
				else if(type==Type.QUERY) return (QueryReply)stub.query((QueryArg)arg);
				else if(type==Type.MOVE) stub.move((MoveArg)arg);
				else if(type==Type.FETCH) return (FetchReply)stub.fetch((FetchArg)arg);
				else System.out.println("operation type exception...");
			} catch(Exception e){
				continue;
			}
			break;
		}
	}

	//periodically check configuration with sharded master
	public void tick(){
		Operation op=new Operation(Type.QUERY,this.gid,null,0);
		this.agree(op);
		Configuration newConfig=this.call(Type.QUERY,new QueryArg(-1));
		if(newConfig.configNumber=this.config.configNumber) return;
		List<Integer> gained=new ArrayList<>();
		int[] shards=newConfig.getShards();
		for(int i=0;i<shards.length;i++){
			if(this.config.shards[i]!=this.gid && shards[i]==this.gid){
				if(this.config.shards[i]>0)	
					gained.add(i);
			}
		}
		if(gained.size()==0) return;
		Map<String,String> newData=new HashMap<>();
		Map<Integer,String> newPreReply=new HashMap<>();
		if(!this.dead){
			for(int shard:gained){
				int otherG=this.config.shards[shard];
				List<String> servers=this.config.groups.get(otherG);
				FetchArg arg=new FetchArg(newConfig.configNumber,shard);
				while(!this.dead){
					boolean b=false;
					for(int i=0;i<servers.size();i++){
						FetchReply fr=this.call(servers[i],Type.FETCH,arg);
						if(fr.err==Err.OK){
							for(String key:fr.data.keySet()){
								newData.put(key,fr.data.get(key));
							}
							for(int key:fr.preReply.keySet()){
								newPreReply.put(key,fr.preReply.get(key));
							}
							b=true;
							break;
						}
						else 
							System.out.println("fetch failed");
					}
					if(b) break;
					Thread.sleep(10);
				}
			}
		}
		this.proposeConfig(newConfig.configNumber,newData,newPreReply);
	}	

	//fetch gained data
	public FetchReply fetch(FetchArg arg){
		if(this.config.configNumber<arg.configNum) return new FetchReply(null,Err.ErrNoKey);
		Map<String,String> data=new HashMap<>();
		for(String key:this.store.keySet()){
			if(key2shard(key)==arg.shard) data.put(key,this.store.get(key));
		}
		return new FetchReply(data,Err.OK,this.preReply);
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
			this.agree(new ClientOp(arg.key,null,Type.GET,arg.rid,arg.me));
			this.seen.put(arg.me,arg.rid);
			return new GetReply(store.get(key),true);
		}
	}
	//wait for paxos to reach agreement
	public ClientOp reachAgreement(int seq){
		while(true){
			Status s=this.paxos.getStatus(seq);
			if(s==Status.DECIDED){
				return (ClientOp)this.paxos.getOperation(seq);
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
	public void commitOperation(int seq,ClientOp op){
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
	public void agree(ClientOp op){
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
		this.agree(new ClientOp(arg.key,arg.value,arg.flag,arg.rid,arg.me));
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

