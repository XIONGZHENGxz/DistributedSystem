package uta.shan.ds.shardmaster;
public class ShardMaster{
	int me;
	Paxos paxos;
	Configuration[] configs;
	private static final int shardsNumber=10;
	public ShardMaster(int mei,String[] servers,int[] ports){
		this.me=me;
		paxos=new Paxos(servers,ports);
		configs=new Configuration[10];
	}

	//join rpc
	public JoinReply join(JoinArg arg){
	}
	
	//leave rpc
	public LeaveReply leave(LeaveArg arg){
	}

	//move rpc
	public MoveReply move(MoveArg arg){
	}

	//query rpc
	public QueryReply query(QueryArg arg){
	}

}
