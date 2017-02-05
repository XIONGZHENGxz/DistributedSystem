import java.rmi.RemoteException;
import java.util.Map;
import java.rmi.Remote;
public interface ServerBase extends Remote{
	public GetReply Get(GetArg arg) throws RemoteException,NotFoundException;
	public PutAppendReply PutAppend(PutAppendArg arg) throws RemoteException;
	public void CopyStore(Map<String,String> map) throws RemoteException;
	public void ForwardRequest(String request,Object arg) throws RemoteException;
	public void Shutdown() throws RemoteException;
	public void Resume() throws RemoteException;
}
