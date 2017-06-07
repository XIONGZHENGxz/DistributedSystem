package uta.shan.paxos2;

/**
 * Created by xz on 6/2/17.
 */
public class PrepareRequest {
    private int seq;
    private int pNumber;

    public PrepareRequest(int seq, int pNumber) {
        this.seq = seq;
        this.pNumber = pNumber;
    }

    public int getSeq() {
        return this.seq;
    }

    public int getNum() {
        return this.pNumber;
    }
}
