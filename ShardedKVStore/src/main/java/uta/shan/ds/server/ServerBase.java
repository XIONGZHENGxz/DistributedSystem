package uta.shan.ds.server;

import java.rmi.RemoteException;
import java.rmi.Remote;

import uta.shan.ds.common.GetReply;
import uta.shan.ds.common.GetArg;
import uta.shan.ds.common.PutAppendReply;
import uta.shan.ds.common.PutAppendArg;

public interface ServerBase extends Remote{
	public GetReply Get(GetArg arg) throws RemoteException;
	public PutAppendReply PutAppend(PutAppendArg arg) throws  RemoteException;
	public void shutdown() throws RemoteException;
	public void resume() throws RemoteException;
	public FetchReply fetch(FetchArg arg) throws RemoteException;
}
