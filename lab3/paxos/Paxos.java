import java.util.Map;
import java.util.HashMap;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
public class Paxos implements PaxosBase{
	String[] peers;
	int me;
	private static final int port=1099;
	Map<Integer,Instance> map;	
	public Paxos(int me,String[] peers){
		this.me=me;
		this.peers=peers;
		map=new HashMap<>();
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
				callReply=stub.ProcessDecision(arg);
			else
				System.out.println("wrong parameter!");
		} catch(Exception e){
			e.printStackTrace();
		}
		return callReply;
	}
	
	//process prepare rpc
	public PaxosReply ProcessPrepare(PaxosArg arg){
		PaxosReply reply=null;
		return reply;
	}

	//process accept rpc
	public PaxosReply ProcessAccept(PaxosArg arg){
		PaxosReply reply=null;
		return reply;
	}
	
	//make decision
	public PaxosReply ProcessDecision(PaxosArg arg){
		PaxosReply reply=null;
		return reply;
	}

	//prepare a proposal
	public PaxosReply prepare(int id,int seq,String peer,String pNum){
		PaxosReply reply=null;
		PaxosArg arg=new PaxosArg(seq,null,pNum,this.me,0);
		if(id==this.me){
			reply=this.ProcessPrepare(arg);
		} else{
			reply=this.call("prepare",arg);
		}
		return reply;
	}
	
	//start agreement
	public void start(){
	}

	//the application on this machine is done with all instances <=seq
	public void Done(int seq){
	}

	//the application wants to know the highest instance seq known 
	//to this peer
	public int Max(){
		return 0;
	}

	//application will forget sequence that is smaller than Min()
	public int Min(){
		return 0;
	}
	
	//the status of agreement of this peer
	public Status Status(){
		return Status.PENDING;
	}
}
