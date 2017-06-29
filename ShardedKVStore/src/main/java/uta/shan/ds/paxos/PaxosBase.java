package uta.shan.ds.paxos;
import java.rmi.Remote;
import java.rmi.RemoteException;
public interface PaxosBase extends Remote{
	public PaxosReply ProcessPrepare(PaxosArg arg) throws RemoteException;
	public PaxosReply ProcessAccept(PaxosArg arg) throws RemoteException;
	public void ProcessDecision(PaxosArg arg) throws RemoteException;
}
	
