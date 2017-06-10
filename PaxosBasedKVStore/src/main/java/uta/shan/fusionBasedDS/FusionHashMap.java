package uta.shan.fusionBasedDS;

import java.util.Map;
import java.util.HashMap;

public class FusionHashMap {
    private Map<Integer,PrimaryNode<Integer>> map;
    private DoubleLinkedList<Integer> auxList;
//    private int me;//my id

    public FusionHashMap() {
        map = new HashMap<>();
        auxList = new DoubleLinkedList<>();
//        me = id;
    }

    //return old value, if not exists, return -1;
    public int put(int key, int value) {
        if(map.containsKey(key)) {
            PrimaryNode<Integer> p = map.get(key);
            int old = p.getValue();
            p.setValue(value);
            return old;
        } else {
            PrimaryNode<Integer> p = new PrimaryNode<>(value);
            map.put(key,p);
            auxList.add(p.getAuxNode());
            return -1;
        }
    }

    //remove
    public boolean remove(int key) {
        if(!map.containsKey(key)) return false;
        AuxNode<Integer> auxNode = map.get(key).getAuxNode();
        auxList.replaceWithTail(auxNode);
        map.remove(key);
        return true;
    }

    public boolean containsKey(int key) {
        return map.containsKey(key);
    }

    public int get(int key) {
        if(!containsKey(key)) return 0;
        return map.get(key).getValue();
    }

    public int getLast() {
        AuxNode<Integer> node = (AuxNode<Integer>) auxList.getTailNode();
        if(node == null) return -1;
        PrimaryNode<Integer> primaryNode = node.getPrimaryNode();
        return primaryNode.getValue();
    }

    public String toString() {
        String res = "auxList:\n\n";
        res += auxList.toString()+"\n\n";
        res += "Fused map:\n\n";
        for(int key: map.keySet()) {
            res += "key: "+key+" value: "+map.get(key).toString()+"\n";
        }
        return res;
    }
}