package uta.shan.paxos2;

import java.util.concurrent.locks.ReentrantLock;
import uta.shan.messager.Messager;

public class Proposer<T>{

	private T value;
	private int me;
	private String[] peers;
	private int[] ports;
	private ReentrantLock lock;
	private Proposal<T> proposal;
	public static int prepareAccNum = 0;
	public static int acceptAccNum = 0;
	public static int maxNum = 0;

	public Proposer(int me, String[] peers,int[] ports) {
		this.me = me;
		this.peers = peers;
		this.ports = ports;
		lock = new ReentrantLock();
		proposal = new Proposal<>();
	}

	//set seq number
	public void setValue(T value) {
		this.value = value;
	}

	//create pnumber
	public int makeNumber() {
		lock.lock();
		Clock clock = Clock.getInstance();
		clock.increment();
		int res = clock.getCount();
		proposal.setNum(res);
		lock.unlock();
		return res;
	}

	//prepare a proposal 
	public void prepare(int seq, String host, int port, int pNum) {
		PrepareRequest request = new PrepareRequest(seq,pNum);
		Messager.sendMsg(request, host, port);

	}

	public void BroadcastPrepare(int seq) {
		int pNumber  = makeNumber();

		for(int i=0;i<peers.length;i++) {
			if(i == me) {
				this.handlePrepareReply(new PrepareReply<T>("ok",proposal.getNum(),value));
			} else {
				prepare(seq, peers[i], ports[i], pNumber);
			}
		}
	}

	//accept request
	public void accept(int seq, String host,int port, Proposal proposal) {
		AcceptRequest<T> acceptRequest = new AcceptRequest<>(seq,proposal);
		Messager.sendMsg(acceptRequest,host,port);
	}

	//broad case accept requests
	public void BroadcaseAccept(int seq,Proposal proposal) {
		for(int i=0;i<peers.length;i++) {
			if(i == me) {
				AcceptReply<T> reply = new AcceptReply<>();
				reply.setStatus("ok");
				this.handleAcceptReply(reply);
			} else {
				accept(seq,peers[i],ports[i], proposal);
			}
		}
	}

	//handle prepare reply
	public void handlePrepareReply(PrepareReply<T> reply) {
		if(reply.getStatus()=="ok") {
			prepareAccNum++;
			if (reply.getNum() > maxNum) {
				maxNum = reply.getNum();
				proposal.setVal(reply.getVal());
			}
		}
	}


	//handle accept reply
	public void handleAcceptReply(AcceptReply<T> reply) {
		if(reply.getStatus()=="ok")
			acceptAccNum++;
	}

	//get Proposal
	public Proposal<T> getProposal() {
		return this.proposal;
	}
}



