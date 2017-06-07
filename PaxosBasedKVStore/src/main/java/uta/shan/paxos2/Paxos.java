package uta.shan.paxos2;

/**
 * Created by xz on 6/1/17.
 */

import java.lang.Thread;
import java.util.concurrent.locks.ReentrantLock;

public class Paxos<T> extends Thread{
    private final static long TIMEOUT = System.currentTimeMillis();
    private String[] peers;//all ip address of replications
    private int[] ports;//corresponding ports
    private int myId;
    private Proposer<T> proposer;
    private Acceptor<T> acceptor;
    private Learner<T> learner;
    private Listener<T> listener;

    private int seq;
    private T value;
    private int[] dones;//record the seq number agreed by each peer
    private ReentrantLock lock;

    public Paxos(String[] peers, int[] ports, int id) {
        this.peers = peers;
        this.ports = ports;
        this.myId = id;
        lock = new ReentrantLock();
        listener = new Listener<>(88888,this);
        listener.start();
        learner = new Learner<>(myId,peers,ports);
        proposer = new Proposer<>(myId,peers,ports);
        acceptor = new Acceptor<>(peers,ports,myId,learner.getSeqMap());
    }

   public void startConcensus(int seq,T value) {
        this.seq = seq;
        proposer.setValue(value);
        this.start();
   }

    //reach agreement
    public Object reachAgreement(int seq) {
        Instance<T> inst = learner.getSeqMap().get(seq);
        while(true) {
            if(inst.getStatus()==Status.DECIDED){
                return inst.getProposal().getValue();
            }
        }
    }
    @Override
    public void run() {
        if(seq<learner.minSeq()) return;//this seq has been forgotten
        while(true) {
            //prepare phase
            long startTime = System.currentTimeMillis();
            long currTime = System.currentTimeMillis();
            proposer.BroadcastPrepare(seq);
            boolean ok = false;
            while(currTime - startTime < TIMEOUT) {
                if(proposer.prepareAccNum > peers.length/2) {
                    ok = true;
                    break;
                }
                currTime = System.currentTimeMillis();
            }
            //accept phase
            if(ok) {
                ok = false;
                startTime = System.currentTimeMillis();
                currTime = System.currentTimeMillis();
                proposer.BroadcaseAccept(seq,proposer.getProposal());
                while(currTime - startTime < TIMEOUT) {
                    if(proposer.acceptAccNum > peers.length/2) {
                        ok = true;
                    }
                    currTime = System.currentTimeMillis();
                }
            }
            //decide phase
            if(ok) {
                learner.BroadcastDecision(seq,proposer.getProposal());
                break;
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
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