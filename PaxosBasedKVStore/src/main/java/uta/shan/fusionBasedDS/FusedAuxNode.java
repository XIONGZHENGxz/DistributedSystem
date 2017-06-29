package uta.shan.fusionBasedDS;

import java.io.Serializable;

/**
 * Created by xz on 6/8/17.
 */
public class FusedAuxNode<E> extends Node implements Serializable {
    final static long serialVersionUID=1L;
    private FusedNode<E> fusedNode;

    public FusedAuxNode(FusedNode<E> p) {
        fusedNode = p;
    }

    public FusedNode getFusedNode() {
        return this.fusedNode;
    }

    public void setFusedNode(FusedNode<E> node) {
        this.fusedNode = node;
    }

}
