package uta.shan.paxos2;

import java.util.concurrent.locks.ReentrantLock;
import uta.shan.communication.Messager;
import uta.shan.communication.Util;

public class Proposer<T>{
	private T value;
	private int me;
	private String[] peers;
	private int[] ports;
	private ReentrantLock lock;
	private Proposal<T> proposal;
	private Acceptor<T> acceptor;

	public int prepareAccNum;
	public int acceptAccNum;
	public int maxNum;

	public Proposer(int me, String[] peers,int[] ports, Acceptor<T> acceptor) {
		prepareAccNum = 0;
		acceptAccNum = 0;
		maxNum = 0;
		this.me = me;
		this.peers = peers;
		this.ports = ports;
		this.acceptor = acceptor;
		lock = new ReentrantLock();
		proposal = new Proposal<>();
	}

	//set seq number
	public void setValue(T value) {
		this.value = value;
		proposal.setVal(value);
	}

	//get value
	public T getValue() {
		return this.value;
	}

	public int getPrepareAccNum() {
		return prepareAccNum;
	}

	public int getAcceptAccNum() {
		return acceptAccNum;
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
	public void prepare(int seq, String host, int port, int pNum, int id) {
		PrepareRequest request = new PrepareRequest(seq,pNum,me);
		if(id == me) {
			PrepareReply<T> reply = acceptor.handlePrepare(request);
			handlePrepareReply(reply);
		} else {
			Messager.sendMsg(request, host, port);
		}
	}

	public void BroadcastPrepare(int seq) {
		if(Util.DEBUG) System.out.println(me+" broadcast prepare..."+seq);
		int pNumber  = makeNumber();

		for(int i=0;i<peers.length;i++) {
			prepare(seq, peers[i], ports[i], pNumber, i);
		}
		if(Util.DEBUG) System.out.println(me+" complete broadcast prepare..."+seq);
	}

	//accept request
	public void accept(int seq, String host,int port, Proposal proposal,int id) {
		AcceptRequest<T> acceptRequest = new AcceptRequest<>(seq,proposal,me);
		if(id == me) {
			AcceptReply<T> reply = acceptor.handleAccept(acceptRequest);
			handleAcceptReply(reply);
		} else {
			Messager.sendMsg(acceptRequest, host, port);
			System.out.println(me+ " send accept to "+id);
		}
	}

	//broad case accept requests
	public void BroadcaseAccept(int seq,Proposal proposal) {
		if(Util.DEBUG) System.out.println(me+" broadcast accept..."+seq+" "+proposal.getValue());
		for(int i=0;i<peers.length;i++) {
			accept(seq,peers[i],ports[i], proposal,i);
		}
		if(Util.DEBUG) System.out.println(me+" complete broadcast accept..."+seq);
	}

	//handle prepare reply
	public void handlePrepareReply(PrepareReply<T> reply) {
		if(Util.DEBUG) System.out.println("prepare reply status..."+reply.getStatus()+" "+me);
		if(reply.getStatus() != null && reply.getStatus().equals("ok")) {
			prepareAccNum++;
			if (reply.getNum() > maxNum) {
				maxNum = reply.getNum();
				if(reply.getVal() != null)
					proposal.setVal(reply.getVal());
			}
		}
	}


	//handle accept reply
	public void handleAcceptReply(AcceptReply<T> reply) {
		if(Util.DEBUG) System.out.println("accept reply status..."+reply.getStatus()+" "+me);
		if(reply.getStatus() != null && reply.getStatus().equals("ok"))
			acceptAccNum++;
	}

	//get Proposal
	public Proposal<T> getProposal() {
		return this.proposal;
	}

	//reset
	public void reset() {
		prepareAccNum = 0;
		acceptAccNum = 0;
		maxNum = 0;
	}
}



