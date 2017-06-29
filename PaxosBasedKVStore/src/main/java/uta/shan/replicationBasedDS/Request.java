package uta.shan.replicationBasedDS;

import java.io.Serializable;

/**
 * Created by xz on 6/10/17.
 */
public class Request<K,V> implements Serializable {
    private final static long serialVersionUID=11L;
    private K key;
    private V value;
    private String rid;
    private String type;

    public Request(K key,V value, String rid, String type) {
        this.key = key;
        this.value = value;
        this.rid = rid;
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public K getKey() {
        return this.key;
    }

    public V getValue() {
        return this.value;
    }

    public String getRid() {
        return this.rid;
    }
}
