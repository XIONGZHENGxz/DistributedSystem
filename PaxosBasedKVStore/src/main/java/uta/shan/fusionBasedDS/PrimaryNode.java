package uta.shan.fusionBasedDS;

import java.io.PrintWriter;

/**
 * Created by xz on 6/7/17.
 */
public class PrimaryNode<E> {
    private E value;
    private AuxNode<E> auxNode;

    public PrimaryNode(E value) {
        this.value = value;
        auxNode = new AuxNode(this);
    }

    //get node in aux list
    public AuxNode<E> getAuxNode() {
        return auxNode;
    }

    //get value
    public E getValue() {
        return value;
    }

    //set value
    public void setValue(E value) {
        this.value = value;
    }

    public String toString() {
        return value.toString();
    }
}
