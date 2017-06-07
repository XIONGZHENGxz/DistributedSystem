package uta.shan.paxos2;

/**
 * Created by xz on 6/2/17.
 */
public class AcceptRequest<T> {
    private int seq;
    private Proposal<T> proposal;

    public AcceptRequest(int seq, Proposal<T> proposal) {
        this.seq = seq;
        this.proposal = proposal;
    }

    public int getSeq() {
        return this.seq;
    }

    public Proposal<T> getProposal() {
        return this.proposal;
    }
}

