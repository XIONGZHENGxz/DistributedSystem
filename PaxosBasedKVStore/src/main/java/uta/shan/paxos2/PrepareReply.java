package uta.shan.paxos2;

import java.io.Serializable;

public class PrepareReply<T> implements Serializable{
	static final long serialVersionUID=2L;

	private String status;
	private int highestNumber;//highest proposal number seen so far
	private T highestValue;//highest proposal value seen so far

	public PrepareReply() {}

	public PrepareReply(String status,int pNum,T pVal){
		this.status = status;
		this.highestNumber = pNum;
		this.highestValue = pVal;
	}

	//set status
	public void setStatus(String status) {
		this.status = status;
	}

	//get status
	public String getStatus() {
		return this.status;
	}

	//set pnumber
	public void setNumAndVal(int pNum, T val) {
		this.highestNumber = pNum;
		this.highestValue = val;
	}

	//get number
	public int getNum() {
		return this.highestNumber;
	}

	//get value
	public T getVal() {
		return this.highestValue;
	}

}
