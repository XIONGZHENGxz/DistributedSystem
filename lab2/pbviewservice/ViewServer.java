import java.util.Set;
import java.lang.Thread;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
public class ViewServer implements ViewService{
	String host;
	int port;
	View view;
	Set<String> servers;
	Map<String,Long> lastPingTime;
	public ViewServer(String host,int port){
		this.port=port;
		this.host=host;
		servers=new HashSet<>();
		lastPingTime=new HashMap<>();
	}

		
	public PingReply Ping(PingArg args){
		int viewNum=args.viewNum;
		System.out.println("view number is:  "+viewNum);
		String hostPort=args.hostPort;
		servers.add(hostPort);
		long pingTime=System.currentTimeMillis();
		lastPingTime.put(hostPort,pingTime);
		if(viewNum==0){
			System.out.println(hostPort+"started!");
			if(view!=null){
				if(!view.backup.equals("")) this.servers.add(hostPort);
				else view.backup=hostPort;
			}
			else{ 
				view=new View(viewNum,hostPort,"");
				System.out.println("assign primary to "+hostPort);
			}
		}
		PingReply pr=new PingReply(this.view,false);
		return pr;
	}

	// server dead
	public void DeclareDead(String server){
		System.out.println(server+" is dead!");
		if(server.equals(this.view.primary)){
			System.out.println("primary is dead");
			this.AssignNewPrimary();
		}
		else if(server.equals(this.view.backup)) this.AssignNewBackup();
		else this.servers.remove(server);
		this.lastPingTime.remove(server);
	}

	//backup is dead, assign new backup
	public void AssignNewBackup(){
		if(this.servers.size()==0) this.view.backup="";
		else{
			this.view.backup=this.servers.iterator().next();
			this.servers.remove(this.view.backup);
		}
	}

	//primary is dead, assign new primary 
	public void AssignNewPrimary(){
		if(this.view.backup.equals("")){
			System.out.println("all servers are dead!");
			this.view=null;
			return;
		}
		else{
			this.view.primary=this.view.backup;
		}
		if(this.servers.size()==0){
			this.view.backup="";
		}else {
			this.view.backup=this.servers.iterator().next();
			this.servers.remove(this.view.backup);
		}
	}
	
	public static void main(String...args){
		ViewServer vs=new ViewServer(args[0],Integer.parseInt(args[1]));
		try{
			System.setProperty("java.rmi.server.hostname","192.168.245.146");
			ViewService stub=(ViewService) UnicastRemoteObject.exportObject(vs,0);
			Registry registry=LocateRegistry.createRegistry(vs.port);
			registry.rebind("view service",stub);
			System.out.println("view service is runing");
		} catch(Exception e){
			e.printStackTrace();
		}
		Thread MonitorPing=new MonitorPingThread(vs);
		MonitorPing.start();
	}

}
class MonitorPingThread extends Thread{
	ViewServer vs;
	public MonitorPingThread(ViewServer vs){
		this.vs=vs;
	}
	@Override
	public void run(){
		while(true){
			String deadServer="";
			for(String server:vs.lastPingTime.keySet()){
				long timeNow=System.currentTimeMillis();
				if((timeNow-vs.lastPingTime.get(server))>Common.DeadPings*Common.PingInterval){
					deadServer=server;
					break;
				}
			}
			if(!deadServer.equals("")) vs.DeclareDead(deadServer);
		}
	}
}
		

