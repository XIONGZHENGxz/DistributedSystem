package uta.shan.paxos2;

import java.io.Serializable;

/**
 * Created by xz on 6/2/17.
 */
public class Decision<T> implements Serializable {
    private final static long serialVersionUID=11L;
    private Proposal<T> p;
    private int seq;
    private int done;
    private int me;

    public Decision(int seq,Proposal<T> p,int done,int me) {
        this.p = p;
        this.seq = seq;
        this.done = done;
        this.me = me;
    }

    //get proposal
    public Proposal<T> getProposal() {
        return this.p;
    }

    //get seq
    public int getSeq() {
        return this.seq;
    }

    //get me
    public int getMe() {
        return this.me;
    }

    //get done
    public int getDone() {
        return this.done;
    }

}
