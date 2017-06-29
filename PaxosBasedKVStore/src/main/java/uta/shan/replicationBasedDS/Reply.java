package uta.shan.replicationBasedDS;

import java.io.Serializable;

/**
 * Created by xz on 6/10/17.
 */
public class Reply<V> implements Serializable {
    private final static long serialVersionUID=11L;
    private V value;
    private boolean status;

    public Reply(V val,boolean s) {
        value = val;
        status = s;
    }

    public V getValue() {
        return value;
    }

    public boolean getStatus() {
        return status;
    }
}
