package uta.shan.ds.shardmaster;
public class ShardMasterBase extends Remote{
	public JoinReply join(JoinArg arg) throws RemoteException;
	public MoveReply move(MoveArg arg) throws RemoteException;
	public QueryReply query(QueryArg arg) throws RemoteException;
	public LeaveReply leave(LeaveArg arg) throws RemoteException;
}
