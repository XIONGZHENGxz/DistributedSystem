import java.rmi.RemoteException;
import java.rmi.Remote;
public interface ServerBase extends Remote{
	public GetReply Get(GetArg arg) throws RemoteException,NotFoundException;
	public PutAppendReply PutAppend(PutAppendArg arg) throws RemoteException;
}
