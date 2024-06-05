package deque;
import java.util.Iterator;


public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private static class ListNode<T>{
        private T item;
        private ListNode<T> prev;
        private ListNode<T> next;

        private ListNode(T item, ListNode<T> prev, ListNode<T> next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }
    }

    private int size;
    private final ListNode<T> sentinel;

    //create an empty deque
    public LinkedListDeque() {
        sentinel = new ListNode<T>(null, null, null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        size = 0;
    }

    @Override
    public void addFirst(T item) {
        ListNode<T> new_node = new ListNode<>(item, sentinel, sentinel.next);
        sentinel.next.prev = new_node;
        sentinel.next = new_node;
        size += 1;
    }

    @Override
    public void addLast(T item) {
        ListNode<T> new_node = new ListNode<>(item, sentinel.prev, sentinel);
        sentinel.prev.next = new_node;
        sentinel.prev = new_node;
        size += 1;
    }

    @Override
    public T removeFirst(){
        T removed = null;
        if (size != 0){
            removed = sentinel.next.item;
            sentinel.next = sentinel.next.next;
            sentinel.next.prev = sentinel;
            size -= 1;
        }
        return removed;
    }

    @Override
    public T removeLast(){
        T removed = null;
        if (size != 0){
            removed = sentinel.prev.item;
            sentinel.prev.prev.next = sentinel;
            sentinel.prev = sentinel.prev.prev;
            size -= 1;
        }
        return removed;
    }

    @Override
    public void printDeque(){
        ListNode<T> p = this.sentinel.next;
        while (p.next != sentinel){
            System.out.print(p.item + " ");
            p = p.next;
        }
        System.out.println();
    }

    @Override
    public T get(int index){
        T result = null;
        if (index <= size -1 && index >= 0){
            ListNode<T> p = this.sentinel;
            for (int i = 0; i <= index; ++i){
                p = p.next;
            }
            result = p.item;
        }
        return result;
    }

    //helper function for getRecursive
    private T recursiveHelper(ListNode<T> n, int index){
        if (index == 0){
            return n.item;
        }
        return recursiveHelper(n.next, index - 1);
    }

    public T getRecursive(int index){
        if (index <= size -1 && index >= 0){
            return recursiveHelper(sentinel.next, index);
        }
        return null;
    }

    @Override
    public int size(){
        return size;
    }

    @Override
    public boolean equals(Object o){
        if (o == this){
            return true;
        }
        else if (o instanceof Deque){
            Deque<T> other = (Deque<T>) o;
            if (size() == other.size()){
                for (int i = 0; i < size; ++i){
                    if (other.get(i) != get(i)){
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<T> iterator(){
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T>{
        private int pointer;
        public LinkedListDequeIterator(){
            pointer = 0;
        }

        @Override
        public boolean hasNext(){
            return pointer < size;
        }

        @Override
        public T next(){
            T returnVal = get(pointer);
            pointer += 1;
            return returnVal;
        }
    }
}