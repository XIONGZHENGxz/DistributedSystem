package uta.shan.paxos2;

/**
 * Created by xz on 6/2/17.
 */
public class Decision {
    private Proposal p;
    private int seq;
    private int done;
    private int me;

    public Decision(int seq,Proposal p,int done,int me) {
        this.p = p;
        this.seq = seq;
        this.done = done;
        this.me = me;
    }

    //get proposal
    public Proposal getProposal() {
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
