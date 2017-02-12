package uta.shan.ds;
import java.rmi.RemoteException;
import java.util.Map;
import java.rmi.Remote;
public interface ServerBase extends Remote{
	public GetReply Get(GetArg arg) throws RemoteException;
	public PutAppendReply PutAppend(PutAppendArg arg) throws  RemoteException;
	public void shutdown() throws RemoteException;
	public void resume() throws RemoteException;
}
