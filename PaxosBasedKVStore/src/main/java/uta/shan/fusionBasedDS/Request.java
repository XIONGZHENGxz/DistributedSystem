package uta.shan.fusionBasedDS;


import java.io.Serializable;

/**
 * Created by xz on 7/17/17.
 */
public class Request<K,V> implements Serializable {
    final static long serialVersionUID=1L;
    private RequestType type;
    private K key;
    private V first;
    private V second;
    private int id;

    public Request(RequestType type, K key) {
        this(type, key, null, null, -1);
    }

    public Request(RequestType type, K key, V val) {
        this(type, key, val, null, -1);
    }

    public Request(RequestType type, K key, V first, V second, int id) {
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
        return this.id;
    }


    public String toString() {
        return " Request type: "+ type.toString()
                + " key: "+ (key == null ? "null" : key.toString())
                + " first value: "+ (first == null ? "null" : first.toString())
                + " second value: " + (second == null ? "null" :second.toString()) + " Id: " + id;
    }
}
