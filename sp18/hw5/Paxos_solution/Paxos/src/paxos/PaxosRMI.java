package paxos;
/**
 * Created by Colinhu on 10/28/17.
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PaxosRMI extends Remote{
    Response Prepare(Request req) throws RemoteException;
    Response Accept(Request req) throws RemoteException;
    Response Decide(Request req) throws RemoteException;
}
