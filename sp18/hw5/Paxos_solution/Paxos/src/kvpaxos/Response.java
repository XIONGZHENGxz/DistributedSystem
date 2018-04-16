package kvpaxos;

import java.io.Serializable;

/**
 * Created by Colinhu on 10/31/17.
 */
public class Response implements Serializable {
    static final long serialVersionUID=22L;
    String key;
    Integer value;
    boolean ok;

    public Response(){
        this.key = null;
        this.value = null;
        this.ok = false;
    }
}
