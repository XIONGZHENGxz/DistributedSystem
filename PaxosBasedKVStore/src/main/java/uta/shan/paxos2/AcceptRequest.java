package uta.shan.paxos2;

import java.io.Serializable;

/**
 * Created by xz on 6/2/17.
 */
public class AcceptRequest<T> implements Serializable{
    private final static long serialVersionUID=11L;
    private int seq;
    private Proposal<T> proposal;
    private int id;

    public AcceptRequest(int seq, Proposal<T> proposal,int id) {
        this.seq = seq;
        this.proposal = proposal;
        this.id = id;
    }

    public int getSeq() {
        return this.seq;
    }

    public Proposal<T> getProposal() {
        return this.proposal;
    }

    public int getId() {
        return this.id;
    }
}

