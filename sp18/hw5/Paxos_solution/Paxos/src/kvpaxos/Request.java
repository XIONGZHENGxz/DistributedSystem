package kvpaxos;
import java.io.Serializable;

/**
 * Created by Colinhu on 10/31/17.
 */
public class Request implements Serializable {
    static final long serialVersionUID=11L;
    String key;
    Integer value;
    Integer seq;

    public Request(String key, Integer value, Integer seq){
        this.key = key;
        this.value = value;
        this.seq = seq;
    }
}
