package uta.shan.fusionBasedDS;

import java.util.List;
import java.util.Map;

/**
 * Created by xz on 6/8/17.
 */
public class FusedMap<E> {
    private List<Map<Integer,FusedAuxNode>> indexList;
    private FusedNode[] tos;
    private DoubleLinkedList<E> dataStack;
    private int numPrimaries;

    public FusedMap(int numPrimaries) {
        this.numPrimaries = numPrimaries;
    }

    public void put(int key, int newVal, int oldVal, int pid, int bid){
        if(indexList.get(pid).containsKey(key)) {
            FusedNode f = indexList.get(pid).get(key).getFusedNode();
            f.updateCode(oldVal,newVal,pid, bid);
        } else {
            if(oldVal == -1){
                try {
                    new Exception("primary doesn't contain this key,backup does!");
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
            node.increRefCount();
            FusedAuxNode<E> a = new FusedAuxNode<>(node);
            node.insertAuxNode(pid,a);
            indexList.get(pid).put(key,a);
        }
    }

    public boolean remove(int key, int valToRemove, int valOfLast, int bid, int pid) {
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
        tos[pid] = (FusedNode) tos[pid].getPre();
        return true;
    }

    public List<Map<Integer,FusedAuxNode>> getIndexList() {
        return this.indexList;
    }

    public DoubleLinkedList<E> getDataStack() {
        return this.dataStack;
    }
}
