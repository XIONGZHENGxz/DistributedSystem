package kvpaxos;

import java.io.Serializable;

/**
 * Created by Colinhu on 10/31/17.
 */
public class Op implements Serializable{
    static final long serialVersionUID=33L;
    String op;
    int ClientSeq;
    String key;
    Integer value;

    public Op(String op, int ClientSeq, String key, Integer value){
        this.op = op;
        this.ClientSeq = ClientSeq;
        this.key = key;
        this.value = value;
    }
}
