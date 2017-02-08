import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;
public class Paxos implements PaxosBase{
	String[] peers;
	int me;
	private static final int port=1099;
	private Random rand;
	Map<Integer,Instance> map;	
	ReentrantLock lock;
	private int[] dones;
	public Paxos(int me,String[] peers){
		this.me=me;
		this.peers=peers;
		this.rand=new Random();
		map=new HashMap<>();
		lock=new ReentrantLock();
		this.dones=new int[this.peers.length];
	}

	//send all remote procedure calls
	public PaxosReply call(String rmiName,PaxosArg arg){
		PaxosReply callReply=null;
		PaxosBase stub=null;
		try{
			Registry registry=LocateRegistry.getRegistry(peers[arg.me],port);
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
		return new Instance(seq,v,Status.PENDING,0,new Proposal());
	}

	//process prepare rpc
	public PaxosReply ProcessPrepare(PaxosArg arg){
		this.lock.lock();
		PaxosReply reply=new PaxosReply();
		int seq=arg.seq;
		if(!this.map.containsKey(seq)) this.map.put(seq,new Instance(seq,null,Status.PENDING,0,new Proposal(0,null)));
		Instance inst=this.map.get(seq);
		if(arg.pNumber>inst.pNumber){
			reply.status="ok";
			inst.pNumber=arg.pNumber;
			if(inst.accepted!=null){
				reply.pNumber=inst.accepted.pNumber;
				reply.pValue=inst.accepted.pValue;
			}
			this.map.put(seq,inst);
		}
		this.lock.unlock();
		return reply;
	}

	//process accept rpc
	public PaxosReply ProcessAccept(PaxosArg arg){
		lock.lock();
		PaxosReply reply=null;
		if(!this.map.containsKey(arg.seq)) this.map.put(arg.seq,makeInstance(arg.seq,null));
		Instance inst=this.map.get(arg.seq);
		if(arg.pNumber>=inst.pNumber){
			inst.pNumber=arg.pNumber;
			inst.accepted=new Proposal(arg.pNumber,arg.pValue);
			reply.status="ok";
		}
		lock.unlock();
		return reply;
	}
	
	//select majority
	public String[] SelectMajority(){
		int len=this.peers.length;
		int size=len/2+1;
		Set<Integer> targets=new HashSet<>();
		String[] acceptors=new String[size];
		size=size+rand.nextInt(len-size);
		for(int i=0;i<size;i++){
			while(true){
				int t=rand.nextInt(len);
				if(!targets.add(t)) continue;
				acceptors[i]=this.peers[t];
			}
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
		return 0;
	}
	//broadcast prepare
	public boolean BroadcastPrepare(int seq,Object value,Proposal p,String[] acceptors){
		int pNumber=this.makepNum();
		int accNum=0;
		int maxpNum=0;
		Object maxValue=value;
		for(String acc:acceptors){
			PaxosReply pr=this.prepare(this.me,seq,acc,pNumber);
			if(pr==null)
				return false;
			if(pr.status=="ok"){
				accNum++;
				if(pr.pNumber>maxpNum){
					maxpNum=pr.pNumber;
					maxValue=pr.pValue;
				}
			}
		}
		p=new Proposal(pNumber,maxValue);
		return accNum>this.peers.length/2;
	}

	//Accept
	public PaxosReply accept(PaxosArg arg){
		PaxosReply pr=null;
		if(arg.me==this.me){
			pr=this.ProcessAccept(arg);
		}else{
			pr=this.call("accept",arg);
		}
		return pr;
	}

	//broadcast accept
	public boolean BroadcastAccept(int seq,String[] acceptors,Proposal p){
		PaxosReply pr=null;
		int accNum=0;
		for(String acc:acceptors){
			pr=accept(new PaxosArg(seq,p.pNumber,p.pValue,this.me,0));
			if(pr!=null && pr.status.equals("ok"))
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
		lock.unlock();
	}

	//broacase decision
	public void BroadCastDecision(int seq,Proposal p){
		for(int i=0;i<this.peers.length;i++){
			PaxosArg arg=new PaxosArg(seq,p.pNumber,p.pValue,this.me,0);
			if(i==this.me){
				this.ProcessDecision(arg);
			}else{
				this.call("decision",arg);
			}
		}

	}


	//prepare a proposal
	public PaxosReply prepare(int id,int seq,String peer,int pNum){
		PaxosReply reply=null;
		PaxosArg arg=new PaxosArg(seq,pNum,null,this.me,0);
		if(id==this.me){
			reply=this.ProcessPrepare(arg);
		} else{
			reply=this.call("prepare",arg);
		}
		return reply;
	}
	
	//start agreement
	public void start(int seq,Object v){
		if(seq<this.Min()) return;
		while(true){
			try{
				Thread.sleep(10);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
			String[] acceptors=this.SelectMajority();
			Proposal p=new Proposal();
			boolean ok=this.BroadcastPrepare(seq,v,p,acceptors);
			if(ok) ok=this.BroadcastAccept(seq,acceptors,p);
			if(ok){
				this.BroadCastDecision(seq,p);
				break;
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
		return 0;
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

	//shutdown itself
	public void shutdown(){
	}

	//restart
	public void resume(){
	}
}
