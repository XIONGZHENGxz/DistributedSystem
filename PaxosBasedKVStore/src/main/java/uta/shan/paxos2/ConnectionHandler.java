package uta.shan.paxos2;

/**
 * Created by xz on 6/2/17.
 */


import java.lang.Thread;
import java.net.Socket;
import uta.shan.messager.Messager;

public class ConnectionHandler<T> extends Thread {
    private Socket socket;
    private Paxos<T> paxos;

    public ConnectionHandler(Socket s, Paxos<T> paxos) {
        socket = s;
        this.paxos = paxos;
    }

    @Override
    public void run() {
        Object msg = Messager.getMsg(socket);
        Acceptor<T> acceptor = paxos.getAcceptor();
        Proposer<T> proposer = paxos.getProposer();
        Learner<T> learner = paxos.getLearner();
        if(msg instanceof PrepareRequest) {
            acceptor.handlePrepare((PrepareRequest) msg);
        } else if(msg instanceof AcceptRequest) {
            acceptor.handleAccept((AcceptRequest<T>) msg);
        } else if(msg instanceof PrepareReply) {
            proposer.handlePrepareReply((PrepareReply<T>) msg);
        } else if(msg instanceof AcceptReply) {
            proposer.handleAcceptReply((AcceptReply<T>) msg);
        } else if(msg instanceof Decision) {
            learner.makeDecision((Decision) msg);
        } else {
            System.out.println("Unknown message type...");
        }
    }
}
