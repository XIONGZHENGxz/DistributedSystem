package uta.shan.ds.shardmaster;

import uta.shan.ds.paxos.Status;
import uta.shan.ds.paxos.Proposal;
import uta.shan.ds.paxos.Paxos;
import uta.shan.ds.paxos.PaxosReply;
import uta.shan.ds.paxos.PaxosArg;
import uta.shan.ds.paxos.Instance;

import uta.shan.ds.common.Type;
import uta.shan.ds.common.Operation;
import uta.shan.ds.common.JoinArg;
import uta.shan.ds.common.JoinReply;
import uta.shan.ds.common.LeaveArg;
import uta.shan.ds.common.LeaveReply;
import uta.shan.ds.common.MoveArg;
import uta.shan.ds.common.MoveReply;
import uta.shan.ds.common.QueryArg;
import uta.shan.ds.common.QueryReply;
import uta.shan.ds.common.Configuration;

import java.util.Map;
import java.util.HashMap;		
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.lang.Runnable;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

public class ShardMaster implements Runnable,ShardMasterBase{
	int me;
	Paxos paxos;
	List<Configuration> configs;
	int[] ports;
	String[] servers;
	int seed;
	private static final int shardsNumber=10;
	public ShardMaster(int me,String[] servers,int[] ports){
		this.seed=0;
		this.me=me;
		this.servers=servers;
		this.ports=ports;
		paxos=new Paxos(me,servers,ports);
		configs=new ArrayList<>();
	}

	//agree on an operation
	public void agree(Operation op){
		this.seed++;
		Status s=this.paxos.getStatus(this.seed);
		if(s==Status.PENDING){
			this.paxos.start(this.seed,op);
			this.commitOperation(this.seed,op);
		}else if(s==Status.DECIDED){
			this.commitOperation(this.seed,op);
		}
	}

	//reach agreement 
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

	//commit an operation
	public void commitOperation(int seq,Operation op){
		if(op.type.equals(Type.JOIN)) this.createConfig(Type.JOIN,op.gid,op.servers,-1);
		else if(op.type.equals(Type.LEAVE)) this.createConfig(Type.LEAVE,op.gid,null,-1);
		else if(op.type.equals(Type.MOVE)) this.createConfig(Type.MOVE,op.gid,null,op.shard);
		else;
		this.paxos.Done(seq);
	}

	//create new configuration
	public void createConfig(Type type,int gid,List<String> servers,int shard){
		Configuration oldConfig=this.configs.get(this.configs.size()-1);
		Configuration newConfig=new Configuration();
		Set<Integer> gids=new HashSet<>();
		Map<Integer,List<String>> oldMap=oldConfig.getGroup();
			switch(type){
			case JOIN:{
				for(int oldGid: oldMap.keySet()){
					gids.add(oldGid);
					newConfig.getGroup().put(oldGid,oldMap.get(oldGid));
				}
				gids.add(gid);
				if(oldMap.containsKey(gid)){
					oldMap.get(gid).addAll(servers);
					newConfig.getGroup().put(gid,oldMap.get(gid));
				}
				else 
					newConfig.getGroup().put(gid,new ArrayList<>(servers));
				newConfig.setShards(this.balance(gids,oldConfig.getShards()));
				break;
				}
			case LEAVE:{
				for(int oldGid:oldMap.keySet()){
					if(oldGid!=gid && oldGid!=0){
						gids.add(oldGid);
						newConfig.getGroup().put(oldGid,oldMap.get(oldGid));
					}
				}
				newConfig.setShards(this.balance(gids,oldConfig.getShards()));
				break;
				}
			case MOVE:{
				int[] shards=oldConfig.getShards();
				for(int i=0;i<shards.length;i++){
					if(shards[i]==shard){
						newConfig.setShard(i,gid);
					}else{
						newConfig.setShard(i,shards[i]);
					}
				}
				for(int oldGid:oldMap.keySet()){
					newConfig.getGroup().put(oldGid,oldMap.get(oldGid));
				}

				break;
				}
			}
		this.configs.add(newConfig);
	}
	
	public int[] balance(Set<Integer> gids,int[] shards){
		int[] newShards=new int[shards.length];
		return newShards;
	}

	//join rpc
	public JoinReply join(JoinArg arg){
		return null;
	}
	
	//leave rpc
	public LeaveReply leave(LeaveArg arg){
		return null;
	}

	//move rpc
	public MoveReply move(MoveArg arg){
		return null;
	}

	//query rpc
	public QueryReply query(QueryArg arg){
		return null;
	}
	
	@Override
	public void run(){
		try{
			System.setProperty("java.rmi.server.hostname",this.servers[this.me]);
			ShardMasterBase stub=(ShardMasterBase) UnicastRemoteObject.exportObject(this,0);
			Registry registry=LocateRegistry.createRegistry(this.ports[this.me]);
			registry.rebind("shardmaster",stub);
			System.out.println("shard master is running....");
		} catch(RemoteException e){
			e.printStackTrace();
		}
	}
}
