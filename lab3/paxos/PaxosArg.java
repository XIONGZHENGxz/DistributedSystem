public class PaxosArg{
	int seq;
	String pNum;
	Object pVal;
	int me;
	int done;
	public PaxosArg(int seq,String pNum,Object pVal,int me,int done){
		this.seq=seq;
		this.pNum=pNum;
		this.pVal=pVal;
		this.me=me;
		this.done=done;
	}
}
