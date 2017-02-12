package uta.shan.paxos;
import java.io.Serializable;
public class PaxosReply implements Serializable{
	static final long serialVersionUID=2L;
	String status;
	int pNumber;//highest number seen so far
	Object pValue;//highest proposal value seen so far
	public PaxosReply(){}
	public PaxosReply(String status,int pNum,Object pVal){
		this.status=status;
		this.pNumber=pNum;
		this.pValue=pVal;
	}
}
