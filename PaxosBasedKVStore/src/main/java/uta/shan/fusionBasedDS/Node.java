package uta.shan.fusionBasedDS;

import java.io.Serializable;

/**
 * Created by xz on 6/7/17.
 */
public class Node<E> implements Serializable {
    final static long serialVersionUID=1L;
    private Node next;
    private Node pre;

    protected E value;

    public Node() {}
    public Node(E value) {
        this.value = value;
    }

    //get next
    public Node getNext() {
        return this.next;
    }

    public Node getPre() {
        return this.pre;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public void setPre(Node pre) {
        this.pre = pre;
    }

    public E getValue() {
        return this.value;
    }

}
