package uta.shan.fusionBasedDS;

import java.io.Serializable;

/**
 * Created by xz on 6/8/17.
 */
public class FusedNode<E> extends Node implements Serializable {
    final static long serialVersionUID=1L;
    private int refCount;
    private FusedAuxNode[] auxNodes;

    public FusedNode(int numOfPrimaries) {
        super();
        value = 0;
        auxNodes = new FusedAuxNode[numOfPrimaries];
    }

    public void increRefCount() {
        this.refCount++;
    }

    public void decreRefCount() {
        this.refCount--;
    }

    public int getRefCount() {
        return this.refCount;
    }

    public boolean isEmpty() {
        return this.refCount == 0;
    }

    public void updateCode(int oldVal, int newVal, int pid, int bid) {
        value = Fusion.updateCode(value,oldVal,newVal,pid,bid);
    }

    /*
    public void removeElem(int oldElem, int pid, int bid) {
        codeVal = Fusion.updateCode(codeVal, oldElem, 0, pid, bid);

    }

    public void addElem(int newElem, int pid, int bid) {
        codeVal = Fusion.updateCode(codeVal,0, newElem, pid, bid);
    }

*/
    public FusedAuxNode<E> getAuxNode(int i) {
        return this.auxNodes[i];
    }

    public void insertAuxNode(int i,FusedAuxNode<E> node) {
        this.auxNodes[i] = node;
    }

}
