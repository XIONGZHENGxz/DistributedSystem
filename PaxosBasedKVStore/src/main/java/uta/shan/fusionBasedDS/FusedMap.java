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
public class FusedMap<E> implements Serializable {
    final static long serialVersionUID=1L;
    private List<Map<Integer,FusedAuxNode>> indexList;
    private FusedNode[] tos;
    private DoubleLinkedList<E> dataStack;
    private int numPrimaries;

    public FusedMap(int numPrimaries) {
        this.numPrimaries = numPrimaries;
        indexList = new ArrayList<>();
        for(int i=0;i<numPrimaries;i++) indexList.add(new HashMap<Integer, FusedAuxNode>());
        tos = new FusedNode[numPrimaries];
        dataStack = new DoubleLinkedList<>();
        this.numPrimaries = numPrimaries;
    }

    public void put(int key, int newVal, int oldVal, int pid, int bid){
        if(indexList.get(pid).containsKey(key)) {
            FusedNode f = indexList.get(pid).get(key).getFusedNode();
            f.updateCode(oldVal,newVal,pid, bid);
        } else {
            if(oldVal != -1){
                try {
                    new Exception("primary contains this key,backup doesn't!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            FusedNode<E> node;
            if(dataStack.isEmpty() || dataStack.getTailNode() == tos[pid]) {
                node = new FusedNode<>(numPrimaries);
                dataStack.add(node);
            } else if(tos[pid] == null) {
                node = (FusedNode<E>) dataStack.getHeadNode();
            } else {
                node = (FusedNode<E>) tos[pid].getNext();
            }
            tos[pid] = node;
            node.updateCode(0,newVal, pid, bid);
            System.out.println("node: "+node.getValue());
            node.increRefCount();
            FusedAuxNode<E> a = new FusedAuxNode<>(node);
            node.insertAuxNode(pid,a);
            indexList.get(pid).put(key,a);
        }
    }

    public boolean remove(int key, int valToRemove, int valOfLast, int bid, int pid) {
        System.out.println("bid: "+bid+" "+indexList.get(pid).containsKey(key));
        if(!indexList.get(pid).containsKey(key)) return false;
        FusedAuxNode<E> fusedAuxNode = indexList.get(pid).remove(key);
        FusedNode<E> fusedNode = fusedAuxNode.getFusedNode();
        if(fusedNode != tos[pid]) {
            fusedNode.updateCode(valToRemove,valOfLast,pid,bid);
            FusedAuxNode<E> lastAuxNode = tos[pid].getAuxNode(pid);
            lastAuxNode.setFusedNode(fusedNode);
            fusedNode.insertAuxNode(pid,lastAuxNode);
            tos[pid].insertAuxNode(pid, null);
        }
        tos[pid].updateCode(valOfLast,0,pid, bid);
        tos[pid].decreRefCount();
        if(tos[pid].isEmpty()) {
            dataStack.pop();
        }
        tos[pid] = tos[pid].getPre()==dataStack.getHead()?null:((FusedNode<E>) tos[pid].getPre());
        return true;
    }

    public List<Map<Integer,FusedAuxNode>> getIndexList() {
        return this.indexList;
    }

    public E get(int pid, int key) {
        FusedAuxNode<E> auxNode = indexList.get(pid).get(key);
        if(auxNode == null) return null;

        FusedNode<E> node = auxNode.getFusedNode();
        if(Util.DEBUG) {
            System.out.println("Debug: "+"pid: "+pid+" key: "+key+" val: "+node.getValue());
        }
        return (E) node.getValue();
    }

    public DoubleLinkedList<E> getDataStack() {
        return this.dataStack;
    }
}
