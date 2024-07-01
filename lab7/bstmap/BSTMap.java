package bstmap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V>{
    private class Node {
        private final K key;
        private final V value;
        private Node left;
        private Node right;

        private Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.left = null;
            this.right = null;
        }
    }

    private Node root;
    private int size;

    public BSTMap() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        Node current = root;
        while (current != null) {
            if (current.key.equals(key)) {
                return true;
            }
            if (current.key.compareTo(key) < 0) {
                current = current.right;
            } else {
                current = current.left;
            }
        }
        return false;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }


    @Override
    public V get(K key) {
        Node current = root;
        while (current != null) {
            if (current.key.compareTo(key) == 0) {
                return current.value;
            }
            if (current.key.compareTo(key) < 0) {
                current = current.right;
            } else {
                current = current.left;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        if (! containsKey(key)) {
            root = put(root, key, value);
            size += 1;
        }
    }

    private Node put(Node n, K key, V value) {
        if (n == null) {
            return new Node(key, value);
        } else if (n.key.compareTo(key) == 0) {
            return n;
        } else if (n.key.compareTo(key) < 0) {
            n.right = put(n.right, key, value);
            return n;
        } else {
            n.left = put(n.left, key, value);
            return n;
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<K>();
        addKeys(this.root, keys);
        return keys;
    }

    private void addKeys(Node n, Set<K> keys) {
        if (n != null) {
            keys.add(n.key);
            addKeys(n.left, keys);
            addKeys(n.right, keys);
        }
    }

    public void printInOrder() {
        printInOrder(root);
    }

    private void printInOrder(Node n) {
        if (n != null) {
            printInOrder(n.left);
            System.out.println(n.key);
            printInOrder(n.right);
        }
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        return this.keySet().iterator();
    }
}
