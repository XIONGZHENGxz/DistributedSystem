package uta.shan.ds.shardmaster;

import uta.shan.ds.common.JoinArg;
import uta.shan.ds.common.JoinReply;
import uta.shan.ds.common.QueryReply;
import uta.shan.ds.common.MoveArg;
import uta.shan.ds.common.MoveReply;
import uta.shan.ds.common.LeaveReply;
import uta.shan.ds.common.LeaveArg;
import uta.shan.ds.common.QueryArg;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ShardMasterBase extends Remote{
	public JoinReply join(JoinArg arg) throws RemoteException;
	public MoveReply move(MoveArg arg) throws RemoteException;
	public QueryReply query(QueryArg arg) throws RemoteException;
	public LeaveReply leave(LeaveArg arg) throws RemoteException;
}
