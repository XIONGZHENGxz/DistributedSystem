package paxos;
import java.io.Serializable;

/**
 * Created by Colinhu on 10/29/17.
 */
public class Response implements Serializable{
    static final long serialVersionUID=2L;
    public boolean OK;
    public int N;
    public Object V;

    public Response(){
        this.OK = false;
        this.N = Integer.MIN_VALUE;
        this.V = null;
    }
}