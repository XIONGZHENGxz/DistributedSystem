package uta.shan.fusionBasedDSTest;

/**
 * Created by xz on 6/9/17.
 */
import static org.junit.Assert.*;

import org.junit.Test;
import uta.shan.fusionBasedDS.DoubleLinkedList;
import uta.shan.fusionBasedDS.Node;

public class DoubleLinedListTester {
    @Test
    public void test1() {
        DoubleLinkedList<Integer> list = new DoubleLinkedList<>();
        assertTrue(list.isEmpty()==true);
        assertTrue(list.getHead().getValue()==null && list.getTail().getValue()==null);
        assertTrue(list.getHead().getNext() == list.getTail());
        assertTrue(list.getTail().getPre() == list.getHead());
    }

    @Test
    public void test2() {
        DoubleLinkedList<Integer> list = new DoubleLinkedList<>();
        Node<Integer> node = new Node<>(4);
        list.add(node);
        assertTrue(list.getSize() == 1);
        assertTrue(list.getHeadNode().getValue() == 4);
        assertTrue(list.getTailNode().getValue() == 4);
        list.remove(node);
        assertTrue(list.getSize() == 0);
        assertTrue(list.getHeadNode() == null);
    }

    @Test
    public void test3() {
        DoubleLinkedList<Integer> list = new DoubleLinkedList<>();
        Node<Integer> node = new Node<>(4);
        list.add(node);
        list.add(new Node<Integer>(5));
        list.add(new Node<Integer>(6));
        assertTrue(list.getTailNode().getValue() == 6);
        list.replaceWithTail(list.getHeadNode());
        assertTrue(list.getTailNode().getValue() == 5);
        assertTrue(list.getHeadNode().getValue() == 6);
    }
}
