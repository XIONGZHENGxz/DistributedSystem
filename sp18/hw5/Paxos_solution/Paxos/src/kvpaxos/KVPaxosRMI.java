package kvpaxos;

/**
 * Created by Colinhu on 10/31/17.
 */
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface KVPaxosRMI extends Remote{
    Response Get(Request req) throws RemoteException;
    Response Put(Request req) throws RemoteException;
}
