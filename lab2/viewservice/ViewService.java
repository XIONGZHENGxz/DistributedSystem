import java.rmi.RemoteException;
import java.rmi.Remote;
public interface ViewService extends Remote{
	public void Ping(PingArgs args,PingReply reply) throws RemoteException;
	public void Get(GetArgs args,GetReply reply) throws RemoteException;
}
