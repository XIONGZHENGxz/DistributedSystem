public class Request{
	String request;
	int logicalClock;//lamport clock
	int pid;//process id
	Request(String req,int clock,int id){
		request=req;
		logicalClock=clock;
		pid=id;
	}

	public int compare(Request r){
		if(logicalClock>r.logicalClock) return 1;
		if(logicalClock<r.logicalClock) return -1;
		if(pid<r.pid) return -1;
		if(pid>r.pid) return 1;
		return 0;
	}	

	public int getPid(){
		return pid;
	}

}
