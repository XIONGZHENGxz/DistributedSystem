package paxos;
import java.io.Serializable;

/**
 * Created by Colinhu on 10/29/17.
 */
public class Request implements Serializable{
    static final long serialVersionUID=1L;
    public int seq;
    public int N;
    public Object V;
    public int done;
    public int me;

    public Request(){
        this.seq = -1;
        this.N = Integer.MIN_VALUE;
        this.V = null;
    }

    public Request(int seq, int N, Object V){
        this.seq = seq;
        this.N = N;
        this.V = V;
    }

    public Request(int seq, int N, Object V, int done, int me){
        this.seq = seq;
        this.N = N;
        this.V = V;
        this.done = done;
        this.me = me;
    }
}