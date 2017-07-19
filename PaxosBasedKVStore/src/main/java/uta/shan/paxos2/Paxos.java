package uta.shan.paxos2;

/**
 * Created by xz on 6/1/17.
 */

import uta.shan.communication.Messager;
import uta.shan.communication.Util;

import java.io.IOException;
import java.lang.Thread;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Map;

public class Paxos<T> extends Thread{
    private String[] peers;//all ip address of replications
    private int[] ports;//corresponding ports
    private int myId;
    private Proposer<T> proposer;
    private Acceptor<T> acceptor;
    private Learner<T> learner;
    private Listener<T> listener;
    private CountDownLatch prepareLatch;
    private CountDownLatch acceptLatch;

    private int seq;
    private int[] dones;//record the seq number agreed by each peer
    private ReentrantLock lock;

    public Paxos(String[] peers, int[] ports, int id) {
        this.peers = peers;
        this.ports = ports;
        this.myId = id;
        lock = new ReentrantLock();
        listener = new Listener<>(ports[myId],this);
        listener.start();
        prepareLatch = new CountDownLatch(1);
        acceptLatch = new CountDownLatch(1);
        learner = new Learner<>(myId,peers,ports);
        acceptor = new Acceptor<>(peers,ports,myId,learner.getSeqMap());
        proposer = new Proposer<>(myId,peers,ports,acceptor,prepareLatch,acceptLatch);
    }

    public void startConcensus(int seq,T value) {
        this.seq = seq;
        proposer.setValue(value);
        run();
    }

    public void shutdown() {
        try {
            listener.getServerSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isShutdown() {
        return listener.getServerSocket().isClosed();
    }

    public Listener<T> getListener() {
        return this.listener;
    }

    public void handleRequest(Object msg) {
        if(msg instanceof PrepareRequest) {
            if(Util.DEBUG) System.out.println("received prepare request..."+myId);
            PrepareReply<T> reply = acceptor.handlePrepare((PrepareRequest) msg);
            PrepareRequest request = (PrepareRequest) msg;
            int id = request.getId();
            Messager.sendMsg(reply,peers[id],ports[id]);
        } else if(msg instanceof AcceptRequest) {
            AcceptRequest<T> request = (AcceptRequest<T>) msg;
            int id = request.getId();
            if(Util.DEBUG) System.out.println(myId+" received accept request from "+id);
            AcceptReply<T> reply = acceptor.handleAccept((AcceptRequest<T>) msg);
            Messager.sendMsg(reply,peers[id],ports[id]);
        } else if(msg instanceof PrepareReply) {
            if(Util.DEBUG) System.out.println("received prepare reply..."+myId);
            proposer.handlePrepareReply((PrepareReply<T>) msg);
        } else if(msg instanceof AcceptReply) {
            if(Util.DEBUG) System.out.println("received accept reply..."+myId);
            proposer.handleAcceptReply((AcceptReply<T>) msg);
        } else if(msg instanceof Decision) {
            if(Util.DEBUG) System.out.println("received decision..."+myId);
            learner.makeDecision((Decision) msg);
        } else {
            System.out.println("Unknown message type...");
        }
    }
    //get decided value
    public T getValue(int seq) {
        Map<Integer,Instance<T>> sm = learner.getSeqMap();
        Instance<T> inst = sm.get(seq);
        Proposal<T> p = inst.getProposal();
        if(p == null) return null;
        return p.getValue();
    }

    //reset
    public void reset() {
        prepareLatch = new CountDownLatch(1);
        acceptLatch = new CountDownLatch(1);
        proposer.reset(prepareLatch,acceptLatch);
    }

    @Override
    public void run() {
        if(Util.DEBUG){
            System.out.println("start concensus with req: "+ seq + " value: "+proposer.getValue());
            System.out.println("min seq: "+learner.minSeq());
        }
        /*
        if(seq<learner.minSeq()){
            return;//this seq has been forgotten
        }
        */
        while(true) {
            if(Util.DEBUG)  System.out.println("start prepare phase..."+seq+" "+proposer.getValue());
            reset();

            //prepare phase
            proposer.BroadcastPrepare(seq);
            boolean ok = false;
            try {
                ok = prepareLatch.await(Util.PAXOS_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!ok && Util.DEBUG) System.out.println("prepare time out "+proposer.getPrepareAccNum());
            if(ok && Util.DEBUG) System.out.println("prepare phase ok");

            //accept phase
            if(ok) {
                ok = false;
                proposer.BroadcaseAccept(seq,proposer.getProposal());
                try {
                    ok = acceptLatch.await(Util.PAXOS_TIMEOUT,TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //decide phase
            if(!ok && Util.DEBUG) System.out.println("accept time out "+proposer.getAcceptAccNum());
            if(ok) {
                if(Util.DEBUG) System.out.println("accept phase ok..." + seq+" "+proposer.getProposal().getValue());
                learner.BroadcastDecision(seq,proposer.getProposal());
                return;
            }
        }
    }

    //get acceptor
    public Acceptor<T> getAcceptor() {
        return this.acceptor;
    }

    //get proposer
    public Proposer<T> getProposer() {
        return this.proposer;
    }

    //get learner
    public Learner<T> getLearner() {
        return this.learner;
    }

}