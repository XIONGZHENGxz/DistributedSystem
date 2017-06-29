package uta.shan.ds.paxos;
import java.util.concurrent.locks.ReentrantLock;
public class Clock{
	private int count;
	static Clock clock=null;
	ReentrantLock lock;
	public Clock(){
		count=0;
		lock=new ReentrantLock();
	}
	
	public static Clock getInstance(){
		if(clock==null)
			clock=new Clock();
		return clock;
	}

	public void reset(){
		this.count=0;
	}

	public void increament(){
		lock.lock();
		this.count++;
		lock.unlock();
	}

	public int getCount(){
		return this.count;
	}
}
	
		
