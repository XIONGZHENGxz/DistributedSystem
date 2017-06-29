package uta.shan.paxos;
public class Proposal<T>{
	int pNumber;
	T pValue;
	boolean bool=false;
	public Proposal(){}
	public Proposal(int number, T value){
		pNumber=number;
		pValue=value;
	}

	public Proposal(int number,T value,boolean b){
		this.pNumber=number;
		this.pValue=value;
		this.bool=b;
	}
	
	//get number
	public int getNum() {
		return this.pNumber;
	}

	//return value
	public T getValue(){
		return this.pValue;
	}
}
