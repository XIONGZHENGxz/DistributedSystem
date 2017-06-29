package uta.shan.paxos2;

import java.io.Serializable;

/**
 * Created by xz on 6/2/17.
 */
public class PrepareRequest implements Serializable {
    private final static long serialVersionUID=11L;
    private int seq;
    private int pNumber;
    private int id;

    public PrepareRequest(int seq, int pNumber, int id) {
        this.seq = seq;
        this.pNumber = pNumber;
        this.id = id;
    }

    public int getSeq() {
        return this.seq;
    }

    public int getNum() {
        return this.pNumber;
    }

    public int getId() {
        return this.id;
    }
}
