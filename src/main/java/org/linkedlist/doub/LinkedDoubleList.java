package org.linkedlist.doub;

public class LinkedDoubleList {

    private final static String SHOW_EMPTY_LIST = "la lista esta vacia";
    private final static String SHOW_NOT_FOUND_DATA = "Dato no encontrado!";

    Node head;
    Node tail;

    public LinkedDoubleList() {
        this.head = null;
        this.tail = null;
    }

    // |100|->|90|->|45|
    public void addFirst(int value) {
        var newNode = new Node(value);
        if (head == null) {
            head = newNode;
            head.next = null;
            head.prev = null;
            tail = newNode;
        } else {
            if (tail.prev == null) {
                tail.prev = newNode;
            }
            head.prev = newNode;
            newNode.next = head;
            head = newNode;
            System.out.println("test");
        }

    }

    public void addEnd(int value) {
        var newNode = new Node(value);
        if (head == null) {
            head = newNode;
            head.next = null;
            head.prev = null;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
    }
    public void deleteHead() {

        if (head == null) {
            System.out.println(SHOW_EMPTY_LIST);
        } else {
            head = head.next;
            head.prev = null;
        }

    }

    public void deleteTail() {

        if (tail == null) {
            System.out.println(SHOW_EMPTY_LIST);
        } else {
            tail = tail.prev;
            tail.next = null;
        }

    }

    public void displayForward() {

        if (head == null) {
            System.out.println(SHOW_EMPTY_LIST);
        } else {
            var currentNode = head;
            while (currentNode != null) {
                System.out.printf(" | %d | -> ", currentNode.value);
                currentNode = currentNode.next;
            }
            System.out.println();
        }

    }

    public void displayBackward() {

        if (tail == null) {
            System.out.println(SHOW_EMPTY_LIST);
        } else {
            var currentNode = tail;
            while (currentNode != null) {
                System.out.printf(" | %d | -> ", currentNode.value);
                currentNode = currentNode.prev;
            }
            System.out.println();
        }

    }

    public Node searchNode(int value) {

        boolean find = false;
        var current = head;

        if (head == null) {
            System.out.println(SHOW_EMPTY_LIST);
        } else {

            while (current != null && !find) {

                if (current.value == value) {
                    System.out.printf("Dato encontrado: %d %n ", current.value);
                    find = true;
                    break;
                }
                current = current.next;
            }

            if (!find) {
                System.out.println(SHOW_NOT_FOUND_DATA);
            }

        }
        return find ? current : null;

    }

    public void updateNode(int currentValue, int newValue) {

        if (head == null) {
            System.out.println(SHOW_EMPTY_LIST);
        } else {
            var current = head;
            boolean find = false;
            while (current != null && !find) {

                if (current.value == currentValue) {
                    System.out.printf("Dato encontrado: %d %n ", current.value);
                    current.value = newValue;
                    find = true;
                    break;
                }
                current = current.next;
            }
            if (!find) {
                System.out.println(SHOW_NOT_FOUND_DATA);
            }
        }

    }
}
