package org.linkedlist.doub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LinkedDoubleListTest {

    private LinkedDoubleList linkedList;

    @BeforeEach
    void setUp() {
        this.linkedList = new LinkedDoubleList();
    }

    @Test
    void addFirst() {
        //given
        //when
        linkedList.addFirst(45);
        linkedList.addFirst(90);
        linkedList.addFirst(100);
        linkedList.displayForward();
        //then
        assertEquals(100, linkedList.head.value);
    }

    @Test
    void addEnd() {
        //given
        //when
        linkedList.addEnd(45);
        linkedList.addEnd(90);
        linkedList.addEnd(100);
        linkedList.displayForward();
        //then
        assertEquals(45, linkedList.head.value);
    }


    @Test
    void deleteHead() {
        //given
        linkedList.addFirst(45);
        linkedList.addFirst(90);
        linkedList.addFirst(100);
        //when
        linkedList.displayForward();
        linkedList.deleteHead();
        linkedList.displayForward();
        //then
        assertEquals(90, linkedList.head.value);
    }

    @Test
    void deleteTail() {
        //given
        linkedList.addFirst(45);
        linkedList.addFirst(90);
        linkedList.addFirst(100);
        //when
        linkedList.displayForward();
        linkedList.deleteTail();
        linkedList.displayForward();
        //then
        assertEquals(90, linkedList.tail.value);
    }

    @Test
    void displayBackward() {
        //given
        linkedList.addFirst(45);
        linkedList.addFirst(90);
        linkedList.addFirst(100);
        //when
        linkedList.displayBackward();
        linkedList.displayForward();
        //then
    }

    @Test
    void searchNode() {
        //given
        linkedList.addFirst(45);
        linkedList.addFirst(90);
        linkedList.addFirst(100);
        //when
        linkedList.displayForward();
        var node = linkedList.searchNode(90);
        //then
        assertEquals(90, node.value);
    }

    @Test
    void updateNode() {
        //given
        linkedList.addFirst(45);
        linkedList.addFirst(90);
        linkedList.addFirst(100);
        //when
        linkedList.displayForward();
        linkedList.updateNode(90, 200);
        var node = linkedList.searchNode(200);
        //then
        linkedList.displayForward();
        assertEquals(200, node.value);
    }
}