package paxos;
import java.io.Serializable;
public class PaxosArg implements Serializable{
	final static long serialVersionUID=1L;
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
