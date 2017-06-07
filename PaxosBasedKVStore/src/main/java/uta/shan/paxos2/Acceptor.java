package uta.shan.paxos2;


import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class Acceptor<T> {
	private ReentrantLock lock;
	private Map<Integer,Instance> seqMap;//sequence -> Instance
	private String[] peers;
	private int[] ports;
	private int me;

	public Acceptor() {}

	public Acceptor(String[] peers, int[] ports, int me, Map<Integer,Instance> seqMap) {
		this.peers = peers;
		this.ports = ports;
		this.me = me;
		this.seqMap = seqMap;
	}

	//handle prepare request
	public PrepareReply handlePrepare(PrepareRequest request) {
		lock.lock();
		PrepareReply reply = new PrepareReply();
		int seq = request.getSeq();
		if(!seqMap.containsKey(seq)) seqMap.put(seq,makeInstance(seq,null));
		Instance inst = seqMap.get(seq);
		if(request.getNum() > inst.getNum()) {
			reply.setStatus("ok");

			//already accepted
			if(inst.getProposal()!=null) {
				reply.setNumAndVal(inst.getNum(),inst.getValue());
			}
			inst.setNum(request.getNum());
			seqMap.put(seq,inst);
		}

		lock.unlock();
		return reply;
	}

	//handle accept request
	public AcceptReply<T> handleAccept(AcceptRequest<T> request) {
		lock.lock();
		AcceptReply<T> reply = new AcceptReply<>();
		int seq = request.getSeq();
		Proposal<T> p = request.getProposal();

		if(!seqMap.containsKey(seq)) seqMap.put(seq,makeInstance(seq,null));
		Instance inst = seqMap.get(seq);
		if(p.getNum() >= inst.getNum()) {
			inst.setNum(p.getNum());
			inst.setProposal(p);
			reply.setStatus("ok");
			seqMap.put(seq,inst);
		}

		lock.unlock();
		return reply;
	}

	public Instance makeInstance(int seq,Object val) {
		return new Instance(seq,val,Status.PENDING,0,null);
	}
}
