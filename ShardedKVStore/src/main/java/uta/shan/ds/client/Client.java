package uta.shan.ds.client;
import uta.shan.ds.common.PutAppendArg;
import uta.shan.ds.common.GetReply;
import uta.shan.ds.common.PutAppendReply;
import uta.shan.ds.common.Type;
import uta.shan.ds.common.GetArg;
import uta.shan.ds.server.ServerBase;

import java.util.Random;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class Client{
	public String[] servers;
	int[] serverPorts;
	Random rand;
	public String me;
	public Client(String name,String[] servers,int[] ports){
		this.me=name;
		this.servers=servers;
		this.serverPorts=ports;
	}
		
	public Object Call(String msg,Object args,String server,int port){
		try{
			Registry registry=LocateRegistry.getRegistry(server,port);
			ServerBase stub=(ServerBase) registry.lookup("key/value store");
			if(msg.equals("get")) return stub.Get((GetArg)args);
			else if(msg.equals("putappend")) return stub.PutAppend((PutAppendArg)args);
			else if(msg.equals("shutdown")) stub.shutdown();
			else if(msg.equals("resume")) stub.resume();
			else System.err.println("parameter error");
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	//generate unique id for every request
	public String makeRequestId(){
		int r=10000000+rand.nextInt(10000000);
		return String.valueOf(r);
	}

	//get
	public String get(String key){
		GetArg ga=new GetArg(key,this.makeRequestId(),this.me);
		GetReply gr=null;
		int i=0;
		for(;i<this.servers.length;i++){
			gr=(GetReply)this.Call("get",ga,this.servers[i],this.serverPorts[i]);
			if(gr.status==true) break;
		}
		if(i==this.servers.length) return "";
		return gr.value;
	}
	
	//shared by put and append
	public PutAppendReply PutAppend(String key,String value,String server,int port,Type flag){
		PutAppendArg paa=new PutAppendArg(this.me,key,value,flag,this.makeRequestId());
		return (PutAppendReply)this.Call("putappend",paa,server,port);
	}

	//shutdown server
	public void shutdown(String server,int port){
		this.Call("shutdown",null,server,port);
	}

	//resume server
	public void resume(String server,int port){
		this.Call("resume",null,server,port);
	}
	
	//put key value
	public void put(String key,String value){
		for(int i=0;i<this.servers.length;i++){
			PutAppendReply par=this.PutAppend(key,value,this.servers[i],this.serverPorts[i],Type.PUT);
			if(par.status==true) break;
		}
	}
	
	//append to key
	public void append(String key,String value){
		for(int i=0;i<this.servers.length;i++){
			PutAppendReply par=this.PutAppend(key,value,this.servers[i],this.serverPorts[i],Type.APPEND);
			if(par.status==true) break;
		}
	}

}




