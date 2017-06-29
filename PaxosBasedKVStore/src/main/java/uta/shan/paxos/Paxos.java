package uta.shan.paxos;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.lang.Runnable;
import java.util.HashSet;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;

public class Paxos implements PaxosBase,Runnable{
	String[] peers;
	int[] ports;
	int me;
	private Random rand;
	Map<Integer,Instance> map;	
	ReentrantLock lock;
	private int[] dones;
	Clock clock;
	int seq;
	Object value;
	PaxosBase stub;
	Registry registry;
	public Paxos(int me,String[] peers,int[] ports){
		this.me=me;
		clock=Clock.getInstance();
		this.peers=peers;
		this.ports=ports;
		this.rand=new Random();
		map=new HashMap<>();
		lock=new ReentrantLock();
		this.dones=new int[this.peers.length];
		for(int i=0;i<this.dones.length;i++) this.dones[i]=Integer.MAX_VALUE;
		try{
			System.setProperty("java.rmi.server.hostname",this.peers[this.me]);
			registry=LocateRegistry.createRegistry(this.ports[this.me]);
			stub=(PaxosBase) UnicastRemoteObject.exportObject(this,0);
			registry.rebind("paxos",stub);
		} catch(RemoteException e){
			e.printStackTrace();
		}
		System.out.println("paxos registered...");
	}
	
	//return instances for test
	public Map<Integer,Instance> getMap(){
		return this.map;
	}
	
	//get value
	public Object getOperation(int seq){
		return this.map.get(seq).getValue();
	}

	//send all remote procedure calls
	public PaxosReply call(String rmiName,PaxosArg arg){
		System.out.println("remote call "+rmiName);
		PaxosReply callReply=null;
		PaxosBase stub=null;
		try{
			Registry registry=LocateRegistry.getRegistry(peers[arg.me],this.ports[arg.me]);
			stub=(PaxosBase) registry.lookup("paxos");
			if(rmiName.equals("prepare"))
				callReply=stub.ProcessPrepare(arg);
			else if(rmiName.equals("accept"))
				callReply=stub.ProcessAccept(arg);
			else if(rmiName.equals("decision")) 
				stub.ProcessDecision(arg);
			else
				System.out.println("wrong parameter!");
		} catch(Exception e){
			e.printStackTrace();
		}
		return callReply;
	}

	// make instance
	public Instance makeInstance(int seq,Object v){
		return new Instance(seq,v,Status.PENDING,0,null);
	}

	//process prepare rpc
	public PaxosReply ProcessPrepare(PaxosArg arg){
		this.lock.lock();

		PaxosReply reply=new PaxosReply();
		int seq=arg.seq;
		if(!this.map.containsKey(seq)) this.map.put(seq,makeInstance(seq,null));
		Instance inst=this.map.get(seq);
		System.out.println("prepare...."+arg.pNumber+"  "+inst.pNumber);
		if(arg.pNumber>inst.pNumber){
			reply.status="ok";
			if(inst.accepted!=null){
				System.out.println("there exist accepted...");
				reply.pNumber=inst.accepted.pNumber;
				reply.pValue=inst.accepted.pValue;
			}

			inst.pNumber=arg.pNumber;
			this.map.put(seq,inst);
		}
		this.lock.unlock();
		return reply;
	}

	//process accept rpc
	public PaxosReply ProcessAccept(PaxosArg arg){
		lock.lock();
		PaxosReply reply=new PaxosReply();
		if(!this.map.containsKey(arg.seq)) this.map.put(arg.seq,makeInstance(arg.seq,null));
		Instance inst=this.map.get(arg.seq);
		System.out.println(arg.pNumber+"  "+inst.pNumber);
		if(arg.pNumber>=inst.pNumber){
			inst.pNumber=arg.pNumber;
			inst.accepted=new Proposal(arg.pNumber,arg.pValue);
			reply.status="ok";
		}
		lock.unlock();
		return reply;
	}
	
	//select majority
	public int[] SelectMajority(){
		int len=this.peers.length;
		int size=len/2+1;
		Set<Integer> targets=new HashSet<>();
		size=size+rand.nextInt(len-size+1);
		int[] acceptors=new int[size];
		for(int i=0;i<size;i++){
			int t=-1;
			while(true){
			    t=rand.nextInt(len);
				if(!targets.add(t)) continue;
				break;
			}
			targets.add(t);
			acceptors[i]=t;
		}
		return acceptors;
	}

	//generate paxos number
	public String GeneratePaxosNumber(){
		String pNum="";
		return pNum;
	}
	//make proposal number
	public int makepNum(){
		lock.lock();
		int num=-1;
		num=clock.getCount();
		clock.increament();
		lock.unlock();
		return num;
	}

	//broadcast prepare
	public Proposal BroadcastPrepare(int seq,Object value,int[] acceptors){
		System.out.println("i am thread "+Thread.currentThread().getName());
		int pNumber=this.makepNum();
		int accNum=0;
		System.out.println("initial p number..."+pNumber);
		int maxpNum=0;
		Object maxValue=value;
		for(int acc:acceptors){
			PaxosReply pr=this.prepare(seq,acc,pNumber);
			if(pr==null)
				return new Proposal(pNumber,maxValue,false);
			if(pr.status!=null && pr.status.equals("ok")){
				accNum++;
				if(pr.pNumber>maxpNum){
					maxpNum=pr.pNumber;
					maxValue=pr.pValue;
				}
			}
		}
		System.out.println("accept number..."+accNum);
		boolean bool=(accNum>this.peers.length/2)?true:false ;
		return new Proposal(pNumber,maxValue,bool);
	}

	//Accept
	public PaxosReply accept(PaxosArg arg){
		PaxosReply pr=new PaxosReply();
		if(arg.me==this.me){
			System.out.println("its me");
			pr=this.ProcessAccept(arg);
		}else{
			pr=this.call("accept",arg);
		}
		return pr;
	}

	//broadcast accept
	public boolean BroadcastAccept(int seq,int[] acceptors,Proposal p){
		PaxosReply pr=null;
		System.out.println("broadcast accept..."+p.pNumber+"..."+p.pValue);
		int accNum=0;
		for(int acc:acceptors){
			pr=accept(new PaxosArg(seq,p.pNumber,p.pValue,acc,0));
			System.out.println("status..."+pr.status);
			if(pr!=null && pr.status !=null && pr.status.equals("ok"))
				accNum++;
		}
		return accNum>this.peers.length/2;
	}

	//make decision
	public void MakeDecision(int seq,Proposal p){
		Instance inst=makeInstance(seq,p.pValue);
		inst.accepted=p;
		inst.status=Status.DECIDED;
		this.map.put(seq,inst);
	}
	
	//process decision
	public void ProcessDecision(PaxosArg arg){
		lock.lock();
		this.MakeDecision(arg.seq,new Proposal(arg.pNumber,arg.pValue));
		this.dones[arg.me]=arg.done;
		lock.unlock();
	}

	//broacase decision
	public void BroadCastDecision(int seq,Proposal p){
		for(int i=0;i<this.peers.length;i++){
			System.out.println("making decision...");
			PaxosArg arg=new PaxosArg(seq,p.pNumber,p.pValue,i,seq);
			if(i==this.me){
				this.ProcessDecision(arg);
				System.out.println("i am...");
			}else{
				this.call("decision",arg);
			}
		}
		System.out.println("decision done....");
		clock.reset();
	}


	//prepare a proposal
	public PaxosReply prepare(int seq,int peer,int pNum){
		PaxosReply reply=null;
		PaxosArg arg=new PaxosArg(seq,pNum,null,peer,0);
		if(peer==this.me){
			System.out.println("its me");
			reply=this.ProcessPrepare(arg);
		} else{
			System.out.println("its not me");
			reply=this.call("prepare",arg);
		}
		return reply;
	}
	
	//start agreement
	public void start(int seq,Object value){
		this.seq=seq;
		this.value=value;
	}

	@Override
	public void run(){
		if(seq<this.Min()) return;
		while(true){
			try{
				Thread.sleep(10);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
			if(this.map.containsKey(seq) && this.map.get(seq).getStatus().equals(Status.DECIDED)) return;
			System.out.println("waiting to decide....");
			int[] acceptors=this.SelectMajority();
			for(int i=0;i<acceptors.length;i++) System.out.println(acceptors[i]);
			Proposal p=this.BroadcastPrepare(this.seq,this.value,acceptors);
			System.out.println("proposal number...value"+p.pNumber+"..."+p.pValue);
			boolean ok=p.bool;
			if(ok){
				System.out.println("prepare ok...");
				ok=this.BroadcastAccept(seq,acceptors,p);
			}
			if(ok){
				System.out.println("accept ok...");
				this.BroadCastDecision(seq,p);
				return;
			}
		}
	}

	//the application on this machine is done with all instances <=seq
	public void Done(int seq){
		if(this.dones[this.me]<seq) this.dones[this.me]=seq;
	}

	//the application wants to know the highest instance seq known 
	//to this peer
	public int Max(){
		int max=0;
		for(int key:this.map.keySet()){
			if(key>max) max=key;
		}
		return max;
	}

	//application will forget sequence that is smaller than Min()
	public int Min(){
		lock.lock();
		int min=Integer.MAX_VALUE;
		for(int done:this.dones){
			if(min>done) min=done;
		}
		for(int seq:this.map.keySet()){
			if(seq<=min && this.map.get(seq).status==Status.DECIDED) this.map.remove(seq);
		}
		lock.unlock();
		return min+1;
	}
	
	//the status of agreement of this peer
	public Status getStatus(int seq){
		lock.lock();
		if(seq<this.Min()){
			lock.unlock();
			return Status.FORGOTTEN;
		}
		if(!this.map.containsKey(seq)){
			lock.unlock();
			return Status.PENDING;
		}
		lock.unlock();
		return this.map.get(seq).status;
	}
	
	//clean up shutdown registry
	public void cleanup(){
		try{
			if(registry!=null) UnicastRemoteObject.unexportObject(registry,true);
		} catch(RemoteException e){
			e.printStackTrace();
		}
	}

	//shutdown itself
	public void shutdown(){
		try{	
			registry.unbind("paxos");
		} catch(RemoteException | NotBoundException e){
			e.printStackTrace();
		}
	}

	//restart
	public void resume(){
		try{	
		registry.rebind("paxos",stub);
		} catch(RemoteException e ){
			e.printStackTrace();
		}
	}
}
