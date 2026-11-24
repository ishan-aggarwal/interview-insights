package com.interviewinsights.company.mongodb._1_coding._lru_cache;

import java.util.HashMap;
import java.util.Map;

class DLLNode {
    int key;
    int value;
    DLLNode prev;
    DLLNode next;

    public DLLNode(int key, int value) {
        this.key = key;
        this.value = value;
        this.prev = null;
        this.next = null;
    }
}

public class LRUCache {

    int capacity;
    int size;
    DLLNode dummyHead;
    DLLNode dummyTail;
    Map<Integer, DLLNode> map;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        this.dummyHead = new DLLNode(-1, -1);
        this.dummyTail = new DLLNode(-1, -1);
        this.map = new HashMap<>();

        dummyHead.next = dummyTail;
        dummyTail.prev = dummyHead;
    }

    public int get(int key) {
        if (!map.containsKey(key)) {
            return -1;
        }
        DLLNode x = map.get(key);
        remove(x);
        addLast(x);
        return x.value;
    }

    public void set(int key, int value) {

        if (map.containsKey(key)) {
            DLLNode x = map.get(key);
            x.value = value;
            remove(x);
            addLast(x);
            return;
        }

        DLLNode newNode = new DLLNode(key, value);

        if (size < capacity) {
            addLast(newNode);
            map.put(key, newNode);
            size++;

        } else if (size == capacity) {

            DLLNode nodeToBeRemoved = dummyHead.next;
            int keyToBeRemoved = nodeToBeRemoved.key;
            remove(nodeToBeRemoved);
            map.remove(keyToBeRemoved);

            addLast(newNode);
            map.put(key, newNode);
        }
    }

    // remove a node from the Doubly linked list
    private void remove(DLLNode x) {
        x.prev.next = x.next;
        x.next.prev = x.prev;
    }

    // insert a node x at the last of the Doubly linked list
    private void addLast(DLLNode x) {
        DLLNode secondLast = dummyTail.prev;

        x.next = dummyTail;
        x.prev = secondLast;

        secondLast.next = x;
        dummyTail.prev = x;
    }

    public static void main(String[] args) {
        // 5 1 S 2 1 G 2 S 3 2 G 2 G 3
        LRUCache cache = new LRUCache(1);

        cache.set(2, 1);
        System.out.println(cache.get(2));

        cache.set(3, 2);
        System.out.println(cache.get(2));

        System.out.println(cache.get(3));
    }
}