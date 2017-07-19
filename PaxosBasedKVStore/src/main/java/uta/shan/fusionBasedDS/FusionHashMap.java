package uta.shan.fusionBasedDS;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

public class FusionHashMap<K, V> implements Serializable {
    final static long serialVersionUID=1L;
    private Map<K,PrimaryNode<V>> map;
    private DoubleLinkedList<V> auxList;

    public FusionHashMap() {
        map = new HashMap<>();
        auxList = new DoubleLinkedList<>();
    }

    //return old value, if not exists, return -1;
    public V put(K key, V value) {
        if(map.containsKey(key)) {
            PrimaryNode<V> p = map.get(key);
            V old = p.getValue();
            p.setValue(value);
            return old;
        } else {
            PrimaryNode<V> p = new PrimaryNode<>(value);
            map.put(key,p);
            auxList.add(p.getAuxNode());
            return null;
        }
    }

    //remove
    public boolean remove(K key) {
        if(!map.containsKey(key)) return false;
        AuxNode<V> auxNode = map.get(key).getAuxNode();
        auxList.replaceWithTail(auxNode);
        map.remove(key);
        return true;
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public V get(K key) {
        if(!containsKey(key)) return null;
        return map.get(key).getValue();
    }

    public V getLast() {
        AuxNode<V> node = (AuxNode<V>) auxList.getTailNode();
        if(node == null) return null;
        PrimaryNode<V> primaryNode = node.getPrimaryNode();
        return primaryNode.getValue();
    }

    public String toString() {
        String res = "";
        res += "Fused map:\n\n";
        for(K key: map.keySet()) {
            res += "key: "+key.toString()+" value: "+map.get(key).toString()+"\n";
        }
        return res;
    }
}