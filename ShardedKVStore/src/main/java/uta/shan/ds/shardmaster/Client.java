package uta.shan.ds.shardmaster;
public class Client{
	String[] servers;
	public Object call(String msg,Object args,String server,int port){
		try{
			Registry registry=LocateRegistry.getRegistry(server,port);
			ServerBase stub=(ServerBase) registry.lookup("shard");
			if(msg.equals("join")) return stub.join((JoinArg)args);
			else if(msg.equals("move")) return stub.move((MoveArg)args);
			else if(msg.equals("leave")) return stub.leave((LeaveArg)args);
			else if(msg.equals("query")) return stub.query((QueryArg)args);
			else if(msg.equals("shutdown")) stub.Shutdown();
			else if(msg.equals("resume")) stub.Resume();
			else System.err.println("parameter error");
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public void join(int gid,String[] servers){
		while(true){
			for(int i=0;i<this.servers.length;i++){
				JoinArg arg=new JoinArg(gid,servers);
				JoinReply jr==this.call("join",arg,this.servers[i],this.ports[i]);
				if(jr.ok)  return;
			}
			try{
				Thread.sleep(20);
			} catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}

	pubic void leave(int gid){
		while(true){
			for(int i=0;i<this.servers.length;i++){
				LeaveArg arg=new LeaveArg(gid);
				LeaveReply lr=this.call("leave",arg,this.servers[i],this.ports[i]);
				if(lr.ok) return;
			}
			Thread.sleep(20);
		}
	}

	public void move(int shard,int gid){
		while(true){	
			for(int i=0;i<this.servers;i++){
				MoveArg arg=new MoveArg(shard,gid);
				MoveReply mr=this.call("move",arg,this.servers[i],this.ports[i]);
				if(mr.ok) return;
			}
			Thread.sleep(20);
		}
	}

	public Configuration query(int num){
		while(true){	
			for(int i=0;i<this.servers;i++){
				QueryArg arg=new QueryArg(shard,gid);
				QueryReply qr=this.call("move",arg,this.servers[i],this.ports[i]);
				if(mr.ok) return qr.config;
			}
			Thread.sleep(20);
		}
	}
}

	
	
