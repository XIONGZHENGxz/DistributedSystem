package uta.shan.paxos2;

import java.io.Serializable;

public class Proposal<T> implements Serializable {
	private final static long serialVersionUID=11L;
	private int pNumber;
	private T pValue;

	public Proposal() {}

	public Proposal(int number, T value) {
		pNumber = number;
		pValue = value;
	}

	//get number
	public int getNum() {
		return this.pNumber;
	}

	//return value
	public T getValue() {
		return this.pValue;
	}

	//set value
	public void setVal(T val) {
		this.pValue = val;
	}

	//set num
	public void setNum(int pNumber) {
		this.pNumber = pNumber;
	}
}
