package uta.shan.paxos2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import uta.shan.communication.Messager;
import uta.shan.communication.Util;

/**
 * Created by xz on 6/2/17.
 */
public class Learner<T> {
    private Map<Integer,Instance<T>> seqMap;
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
        for(int i=0;i<peers.length;i++) dones[i] = -1;
        lock = new ReentrantLock();
    }

    //get status
    public Status getStatus(int seq) {
        if(!seqMap.containsKey(seq)) return Status.PENDING;
        return seqMap.get(seq).getStatus();
    }

    public void BroadcastDecision(int seq,Proposal<T> proposal) {
        if(Util.DEBUG) System.out.println(me+" broadcast decion "+seq+" "+proposal.getValue());
        Decision decision = new Decision(seq,proposal,dones[me],me);
        for(int i=0;i<peers.length;i++){
            if(i == me) {
                makeDecision(decision);
            } else {
                sendDecision(decision,peers[i], ports[i]);
            }
        }
        doneSeq(seq);
        if(Util.DEBUG) System.out.println(me+" complete broadcast decision "+seq+" ");
    }

    public void sendDecision(Decision decision,String host,int port) {
        Messager.sendMsg(decision,host,port);
    }

    public void makeDecision(Decision decision) {
        if(Util.DEBUG) System.out.println(me+" making decision "+decision.getProposal().getValue());
        Proposal<T> p = decision.getProposal();
        int seq = decision.getSeq();
        Instance<T> inst = new Instance<>(seq,p.getValue(),Status.DECIDED,p.getNum(),p);
        dones[decision.getMe()] = decision.getDone();
        seqMap.put(seq,inst);
        if(Util.DEBUG) System.out.println(me+" made decision ");
    }

    public void doneSeq(int seq) {
        if(dones[me] < seq) dones[me] = seq;
    }

    //get seqmap
    public Map<Integer,Instance<T>> getSeqMap() {
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

        /*
        Iterator<Map.Entry<Integer,Instance<T>>> iter = (Iterator<Map.Entry<Integer,Instance<T>>>)seqMap.entrySet().iterator();
        while(iter.hasNext()) {
            int key = iter.next().getKey();
            if(key <= globalMin && seqMap.get(key).getStatus() == Status.DECIDED) iter.remove();
        }
    */
        lock.unlock();
        return globalMin+1;
    }

}





