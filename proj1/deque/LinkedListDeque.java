package deque;

public class LinkedListDeque<T>{
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

    public void addFirst(T item) {
        ListNode<T> new_node = new ListNode<>(item, sentinel, sentinel.next);
        sentinel.next.prev = new_node;
        sentinel.next = new_node;
        size += 1;
    }

    public void addLast(T item) {
        ListNode<T> new_node = new ListNode<>(item, sentinel.prev, sentinel);
        sentinel.prev.next = new_node;
        sentinel.prev = new_node;
        size += 1;
    }

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

    public void printDeque(){
        ListNode<T> p = this.sentinel.next;
        while (p.next != sentinel){
            System.out.print(p.item + " ");
            p = p.next;
        }
        System.out.println();
    }

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
    public T recursiveHelper(ListNode<T> n, int index){
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


    public int size(){
        return size;
    }

    public Boolean isEmpty(){
        return size == 0;
    }
}