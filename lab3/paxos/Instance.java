public class Instance{
	int seq;//instance id
	Object value;
	Status status;
	String pNumber;
	Proposal accepted;
	public Instance(int seq,Object value,Status status,String pNum,Proposal p){
		this.seq=seq;
		this.value=value;
		this.status=status;
		this.pNumber=pNum;
		this.accepted=p;
	}
}
