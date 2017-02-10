package paxos;
public class Proposal{
	int pNumber;
	Object pValue;
	boolean bool=false;
	protected Proposal(){}
	protected Proposal(int number, Object value){
		pNumber=number;
		pValue=value;
	}

	protected Proposal(int number,Object value,boolean b){
		this.pNumber=number;
		this.pValue=value;
		this.bool=b;
	}

	//return value
	public Object getValue(){
		return this.pValue;
	}
}
