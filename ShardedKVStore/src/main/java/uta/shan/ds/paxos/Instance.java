package uta.shan.ds.paxos;
public class Instance{
	int seq;//instance id
	Object value;
	Status status;
	int pNumber;
	Proposal accepted;
	public Instance(int seq,Object value,Status status, int pNum,Proposal p){
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
	
	//return value
	public Object getValue(){
		return this.value;
	}

	//return proposal
	public Proposal getProposal(){
		return this.accepted;
	}
}
