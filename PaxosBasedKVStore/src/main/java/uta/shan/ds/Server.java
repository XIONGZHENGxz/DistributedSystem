package uta.shan.ds;

import java.util.concurrent.locks.ReentrantLock;
import java.util.Map;
import java.util.HashMap;
import uta.shan.paxos2.*;


public class Server {

	private ClientListener listener;
	private String[] servers;
	private int[] ports;
	private int me;
	private int seed;
	private ReentrantLock lock;
	private Paxos<Operation> paxos;
	Map<String,String> store;

	public Server(int me,String[] servers,int[] ports){
		this.seed=0;
		this.me=me;
		this.servers=servers;
		this.ports=ports;
		this.paxos=new Paxos(servers,ports,me);
		store=new HashMap<>();
		lock=new ReentrantLock();
		listener = new ClientListener(99999,this);
		listener.start();
	}

	//get me
	public int getMe() {
		return this.me;
	}

	//read value
	public GetReply get(GetArg arg){
		String key=arg.key;
		agree(new Operation(arg.rid,arg.key,null,"get"));
		if(!store.containsKey(key)) new GetReply(null,false);
		return new GetReply(store.get(key),true);
	}

	//commit operation
	public void commitOperation(int seq,Operation op){
		if(op.type.equals("put")){
			store.put(op.key,op.value);
		}else if (op.type.equals("append")){
			store.put(op.key,store.get(op.key)+op.value);
		}
		paxos.getLearner().doneSeq(seq);
	}

	//agree on an operation
	public void agree(Operation op){
		while(true) {
			seed++;
			Status status = paxos.getLearner().getStatus(seed);
			if (status == Status.PENDING) {
				paxos.startConcensus(seed, op);
				Operation agreed = (Operation) paxos.reachAgreement(seed);
				commitOperation(seed, agreed);
				if(agreed.rid.equals(op.rid)) break;
			} else if (status == Status.DECIDED)
				commitOperation(seed, (Operation) paxos.getLearner().getSeqMap().get(seed).getValue());
		}
	}
		
	//write value
	public PutReply put(PutArg arg){
		lock.lock();
		String key=arg.key;
		String val=arg.value;
		this.agree(new Operation(arg.rid, arg.key,arg.value,"put"));
		lock.unlock();
		return new PutReply(true);
	}

}

