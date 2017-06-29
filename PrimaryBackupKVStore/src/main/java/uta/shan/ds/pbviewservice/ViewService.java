import java.rmi.RemoteException;
import java.rmi.Remote;
public interface ViewService extends Remote{
	public PingReply Ping(PingArg args) throws RemoteException;
}
