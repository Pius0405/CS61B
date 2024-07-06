package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @Pius YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private int size;
    private int initialSize = 16;
    private double maxload = 0.75;

    /** Constructors */
    public MyHashMap() {
        buckets = createTable(initialSize);
        size = 0;
    }

    public MyHashMap(int initialSize) {
        buckets = createTable(initialSize);
        size = 0;
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        buckets = createTable(initialSize);
        this.maxload = maxLoad;
        size = 0;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection[] createTable(int tableSize) {
        Collection[] table = new Collection[tableSize];
        for (int i = 0; i < tableSize; ++i) {
            table[i] = createBucket();
        }
        return table;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!
    private int getTableIndex(K key, int tableSize) {
        int keyHash = key.hashCode();
        int index = Math.floorMod(keyHash, tableSize);
        return index;
    }

    private void resize(){
        double loadFactor = (double) size/buckets.length;
        if (loadFactor > maxload) {
            Collection<Node>[] newBuckets = createTable(buckets.length * 2);
            int index;
            for (int i = 0; i < buckets.length; ++i) {
                for (Node node : buckets[i]) {
                    index = getTableIndex(node.key, newBuckets.length);
                    newBuckets[index].add(node);
                }
            }
            buckets = newBuckets;
        }
    }

    @Override
    public void clear() {
        buckets = createTable(initialSize);
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        int index = getTableIndex(key, buckets.length);
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(K key) {
        int index = getTableIndex(key, buckets.length);
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    @Override
    public int size(){
        return size;
    }

    @Override
    public void put(K key, V value) {
        int index = getTableIndex(key, buckets.length);
        for (Node node : buckets[index]) {
            if (node.key == key) {
                node.value = value;
                return;
            }
        }
        buckets[index].add(createNode(key, value));
        ++size;
        resize();
    }

    @Override
    public Set<K> keySet() {
        HashSet<K> keys = new HashSet<>();
        for (int i = 0; i < buckets.length; ++i) {
            for (Node node : buckets[i]) {
                keys.add(node.key);
            }
        }
        return keys;
    }

    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }

    @Override
    public V remove (K key, V value) {
        int index = getTableIndex(key, buckets.length);
        for (Node node: buckets[index]) {
            if (node.key.equals(key) && node.value.equals(value)) {
                buckets[index].remove(node);
                --size;
                return node.value;
            }
        }
        return null;
    }

    @Override
    public V remove (K key) {
        int index = getTableIndex(key, buckets.length);
        for (Node node: buckets[index]) {
            if (node.key.equals(key)) {
                buckets[index].remove(node);
                --size;
                return node.value;
            }
        }
        return null;
    }
}
