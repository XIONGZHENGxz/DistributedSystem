package uta.shan.paxos;
public class PaxosArg{
	int seq;
	int pNumber;
	Object pValue;
	int me;
	int done;
	public PaxosArg(int seq,int pNum,Object pVal,int me,int done){
		this.seq=seq;
		this.pNumber=pNum;
		this.pValue=pVal;
		this.me=me;
		this.done=done;
	}
}
