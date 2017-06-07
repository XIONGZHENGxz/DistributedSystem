package uta.shan.paxos2;
import java.util.concurrent.locks.ReentrantLock;

public class Clock{
	private int count;
	private static Clock clock=null;
	ReentrantLock lock;

	//singleton class
	private Clock(){
		count=0;
		lock=new ReentrantLock();
	}
	
	public static Clock getInstance(){
		if(clock==null)
			clock=new Clock();
		return clock;
	}

	public void reset() {
		this.count=0;
	}

	public void increment() {
		lock.lock();
		this.count++;
		lock.unlock();
	}

	public int getCount(){
		return this.count;
	}
}
	
		
