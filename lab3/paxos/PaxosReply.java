public class PaxosReply{
	Status status;
	String pNum;//highest number seen so far
	Object pVal;//highest proposal value seen so far
	public PaxosReply(Status status,String pNum,Object pVal){
		this.status=status;
		this.pNum=pNum;
		this.pVal=pVal;
	}
}
