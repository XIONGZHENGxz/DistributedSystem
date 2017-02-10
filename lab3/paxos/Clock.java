package paxos;
public class Clock{
	private int count;
	static Clock clock=null;
	public Clock(){
		count=0;
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
		this.count++;
	}

	public int getCount(){
		return this.count;
	}
}
	
		
