package uta.shan.fusionBasedDS;

/**
 * Created by xz on 6/7/17.
 */
public class AuxNode<E> extends Node {
    private PrimaryNode<E> primaryNode;

    public AuxNode(PrimaryNode node) {
        primaryNode = node;
    }

    //get bounded primary node
    public PrimaryNode<E> getPrimaryNode() {
        return primaryNode;
    }

}
