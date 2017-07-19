package uta.shan.fusionBasedDS;

import uta.shan.communication.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xz on 6/8/17.
 */
public class FusedMap<K,V> implements Serializable {
    final static long serialVersionUID=1L;
    private List<Map<K,FusedAuxNode<V>>> indexList;
    private FusedNode<V>[] tos;
    private DoubleLinkedList<V> dataStack;
    private int numPrimaries;

    public FusedMap(int numPrimaries) {
        this.numPrimaries = numPrimaries;
        indexList = new ArrayList<>();
        for(int i=0;i<numPrimaries;i++) indexList.add(new HashMap<K, FusedAuxNode<V>>());
        tos = new FusedNode[numPrimaries];
        dataStack = new DoubleLinkedList<>();
        this.numPrimaries = numPrimaries;
    }

    public void put(K key, V oldVal, V newVal, int pid, int bid){
        if(indexList.get(pid).containsKey(key)) {
            FusedNode f = indexList.get(pid).get(key).getFusedNode();
            f.updateCode(oldVal,newVal,pid, bid);
        } else {
            if(oldVal != null){
                try {
                    new Exception("primary contains this key,backup doesn't!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            FusedNode<V> node;
            if(dataStack.isEmpty() || dataStack.getTailNode() == tos[pid]) {
                node = new FusedNode<>(numPrimaries);
                dataStack.add(node);
            } else if(tos[pid] == null) {
                node = (FusedNode<V>) dataStack.getHeadNode();
            } else {
                node = (FusedNode<V>) tos[pid].getNext();
            }
            tos[pid] = node;
            node.updateCode(null,newVal, pid, bid);
            node.increRefCount();
            FusedAuxNode<V> a = new FusedAuxNode<>(node);
            node.insertAuxNode(pid,a);
            indexList.get(pid).put(key,a);
        }
    }

    public boolean remove(K key, V valToRemove, V valOfLast, int bid, int pid) {
        if(Util.DEBUG) System.out.println("bid: "+bid+" "+indexList.get(pid).containsKey(key));
        if(!indexList.get(pid).containsKey(key)) return false;
        FusedAuxNode<V> fusedAuxNode = indexList.get(pid).remove(key);
        FusedNode<V> fusedNode = fusedAuxNode.getFusedNode();
        if(fusedNode != tos[pid]) {
            fusedNode.updateCode(valToRemove,valOfLast,pid,bid);
            FusedAuxNode<V> lastAuxNode = tos[pid].getAuxNode(pid);
            lastAuxNode.setFusedNode(fusedNode);
            fusedNode.insertAuxNode(pid,lastAuxNode);
            tos[pid].insertAuxNode(pid, null);
        }
        tos[pid].updateCode(valOfLast,null,pid, bid);
        tos[pid].decreRefCount();
        if(tos[pid].isEmpty()) {
            dataStack.pop();
        }
        tos[pid] = tos[pid].getPre()==dataStack.getHead()?null:((FusedNode<V>) tos[pid].getPre());
        return true;
    }

    public List<Map<K,FusedAuxNode<V>>> getIndexList() {
        return this.indexList;
    }

    public V get(int pid, int key) {
        FusedAuxNode<V> auxNode = indexList.get(pid).get(key);
        if(auxNode == null) return null;

        FusedNode<V> node = auxNode.getFusedNode();
        if(Util.DEBUG) {
            System.out.println("Debug: "+"pid: "+pid+" key: "+key+" val: "+node.getValue());
        }
        return (V) node.getValue();
    }

    public DoubleLinkedList<V> getDataStack() {
        return this.dataStack;
    }
}
