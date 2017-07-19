package uta.shan.fusionBasedDS;

import com.sun.org.apache.regexp.internal.RE;

/**
 * Created by xz on 7/17/17.
 */
public class Update<K,V> {
    private RequestType type;
    private K key;
    private V first;
    private V second;
    private int id;

    public Update(RequestType type, K key) {
    }

    public Update(RequestType type, K key, V first, V second, int id) {
        this.type = type;
        this.key = key;
        this.first = first;
        this.second = second;
        this.id = id;
    }

    public RequestType getType() {
        return type;
    }
    public K getKey() {
        return key;
    }

    public V getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }

    public int getId() {
        return id;
    }
}
