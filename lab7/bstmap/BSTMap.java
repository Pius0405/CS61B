package bstmap;

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
    public Iterator<K> iterator() {
        return this.keySet().iterator();
    }

    @Override
    public V remove(K key) {
        Node delNode = root;
        Node parent = null;
        String pos = null;
        while (delNode != null) {
            if (delNode.key.compareTo(key) == 0) {
                break;
            } else if (delNode.key.compareTo(key) > 0){
                pos = "left";
                parent = delNode;
                delNode = delNode.left;
            } else {
                pos = "right";
                parent = delNode;
                delNode = delNode.right;
            }
        }
        if (delNode != null) {
            if (delNode.left == null && delNode.right == null) {
                if (delNode == root) {
                    root = null;
                } else if (pos.equals("left")) {
                    parent.left = null;
                } else {
                    parent.right = null;
                }
            } else if (delNode.left != null && delNode.right == null) {
                if (delNode == root) {
                    root = delNode.left;
                } else if (pos.equals("left")){
                    parent.left = delNode.left;
                } else {
                    parent.right = delNode.left;
                }
            } else if (delNode.left == null && delNode.right != null) {
                if (delNode == root) {
                    root = delNode.right;
                } else if (pos.equals("left")){
                    parent.left = delNode.right;
                } else {
                    parent.right = delNode.right;
                }
            } else {
                HibbardDeletion(delNode, parent, pos);
            }
            size -= 1;
            return delNode.value;
        }
        return null;
    }

    private void HibbardDeletion(Node delNode, Node parent, String pos) {
        Node predecessor = delNode.left;
        Node predParent = null;
        while (predecessor.right != null) {
            predParent = predecessor;
            predecessor = predecessor.right;
        }
        predecessor.right = delNode.right;
        if (predParent != null) {
            predParent.right = predecessor.left;
            predecessor.left = delNode.left;
        }
        if (delNode == root) {
            root = predecessor;
        } else {
            if (pos.equals("left")) {
                parent.left = predecessor;
            } else {
                parent.right = predecessor;
            }
        }
    }

    @Override
    public V remove(K key, V value) {
        if (get(key).equals(value)) {
            return remove(key);
        }
        return null;
    }
}
