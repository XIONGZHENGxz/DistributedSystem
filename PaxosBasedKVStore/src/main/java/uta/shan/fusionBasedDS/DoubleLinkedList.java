package uta.shan.fusionBasedDS;

import java.io.Serializable;

/**
 * Created by xz on 6/7/17.
 */
public class DoubleLinkedList<E> implements Serializable {
    final static long serialVersionUID=1L;
    private Node head;
    private Node tail;
    private int size;

    public DoubleLinkedList() {
        head = new Node(null);
        tail = new Node(null);
        head.setNext(tail);
        tail.setPre(head);
        size = 0;
    }

    //get head
    public Node<E> getHead() {
        return this.head;
    }

    //get tail
    public Node<E> getTail() {
        return this.tail;
    }

    //add to tail
    public void add(Node<E> node) {
        tail.getPre().setNext(node);
        node.setNext(tail);
        node.setPre(tail.getPre());
        tail.setPre(node);
        size++;
    }

    public boolean remove(Node<E> node) {
        if(node == null || node.getPre() == null || node.getNext() == null) return false;
        node.getPre().setNext(node.getNext());
        node.getNext().setPre(node.getPre());
        size--;
        return true;
    }

    public void replaceWithTail(Node<E> node) {
        Node<E> last = tail.getPre();

        //remove last node
        remove(last);

        //replace
        last.setPre(node.getPre());
        last.setNext(node.getNext());
        node.getPre().setNext(last);
        node.getNext().setPre(last);
    }

    public boolean isEmpty() {
        return size==0;
    }

    public Node<E> getTailNode() {
        if(isEmpty()) return null;
        return this.tail.getPre();
    }

    public Node<E> getHeadNode() {
        if(isEmpty()) return null;
        return this.head.getNext();
    }

    public void pop() {
        this.remove(tail.getPre());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(head.getNext() == tail) return "";
        Node<E> node = getHeadNode();
        while(node != getTail()) {
            sb.append(node.toString()+"\n");
            node = node.getNext();
        }
        return sb.toString();
    }

    public int getSize() {
        return this.size;
    }
}
