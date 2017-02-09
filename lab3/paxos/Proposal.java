package paxos;
public class Proposal{
	int pNumber;
	Object pValue;
	protected Proposal(){}
	protected Proposal(int number, Object value){
		pNumber=number;
		pValue=value;
	}
}
