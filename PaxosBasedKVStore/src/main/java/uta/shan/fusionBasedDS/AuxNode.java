package uta.shan.fusionBasedDS;

import java.io.Serializable;

/**
 * Created by xz on 6/7/17.
 */
public class AuxNode<E> extends Node implements Serializable {
    final static long serialVersionUID=1L;
    private PrimaryNode<E> primaryNode;

    public AuxNode(PrimaryNode node) {
        primaryNode = node;
    }

    //get bounded primary node
    public PrimaryNode<E> getPrimaryNode() {
        return primaryNode;
    }

}
