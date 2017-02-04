package paxos;
public class Paxos{
	String[] peers;
	int me;
	
	public Paxos(){
	}

	//send all remote procedure calls
	public void call(){
	}

	//start agreement
	public void start(){
	}

	//the application on this machine is done with all instances <=seq
	public void Done(int seq){
	}

	//the application wants to know the highest instance seq known 
	//to this peer
	public int Max(){
	}

	//application will forget sequence that is smaller than Min()
	public int Min(){
	}
	
	//the status of agreement of this peer
	public Fate Status(){
	}

}
