package uta.shan.paxos2;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import uta.shan.messager.Messager;

/**
 * Created by xz on 6/2/17.
 */
public class Learner<T> {
    private Map<Integer,Instance> seqMap;
    private String[] peers;
    private int[] ports;
    private int me;
    private int[] dones;
    private ReentrantLock lock;

    public Learner(int me, String[] peers, int[] ports) {
        this.me = me;
        seqMap = new HashMap<>();
        this.peers = peers;
        this.ports = ports;
        dones = new int[peers.length];
        lock = new ReentrantLock();
    }

    //get status
    public Status getStatus(int seq) {
        return seqMap.get(seq).getStatus();
    }

    public void BroadcastDecision(int seq,Proposal<T> proposal) {
        Decision decision = new Decision(seq,proposal,dones[me],me);
        for(int i=0;i<peers.length;i++){
            if(i == me) {
                makeDecision(decision);
            } else {
                sendDecision(decision,peers[i], ports[i]);
            }
        }
        doneSeq(seq);
    }

    public void sendDecision(Decision decision,String host,int port) {
        Messager.sendMsg(decision,host,port);
    }

    public void makeDecision(Decision decision) {
        Proposal<T> p = decision.getProposal();
        int seq = decision.getSeq();
        Instance<T> inst = new Instance<>(seq,p.getValue(),Status.DECIDED,p.getNum(),p);
        dones[decision.getMe()] = decision.getDone();
        seqMap.put(seq,inst);
    }

    public void doneSeq(int seq) {
        if(dones[me] < seq) dones[me] = seq;
    }

    //get seqmap
    public Map<Integer,Instance> getSeqMap() {
        return this.seqMap;
    }

    //get minimum completed seq of all peers
    public int minSeq() {
        lock.lock();
        int globalMin = dones[me];

        for(int i=0;i<dones.length;i++) {
            if(dones[i] < globalMin) {
                globalMin = dones[i];
            }
        }

        for(int key:seqMap.keySet()) {
            if(key <= globalMin && seqMap.get(key).getStatus() == Status.DECIDED)
                seqMap.remove(key);
        }

        lock.unlock();
        return globalMin+1;
    }

}





