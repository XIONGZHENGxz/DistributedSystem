package uta.shan.paxos2;

public class Instance<T>{

	private int seq;//instance id
	private T value;
	private Status status;
	private int pNumber;
	private Proposal<T> accepted;

	public Instance(int seq,T value,Status status, int pNum,Proposal p){
		this.seq=seq;
		this.value=value;
		this.status=status;
		this.pNumber=pNum;
		this.accepted=p;
	}
	

	//return status
	public Status getStatus(){
		return this.status;
	}

	//get pnumber
	public int getNum() {
		return this.pNumber;
	}

	//return value
	public Object getValue(){
		return this.value;
	}

	//return proposal
	public Proposal getProposal(){
		return this.accepted;
	}

	//set proposal
	public void setProposal(Proposal<T> p) {
		this.accepted = p;
	}
	//set pnumber
	public void setNum(int pNUm) {
		this.pNumber = pNUm;
	}
}
