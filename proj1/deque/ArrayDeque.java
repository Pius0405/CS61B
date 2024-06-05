package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] items = (T[])new Object[8];
    private int size;
    private int nextFirst;
    private int nextLast;
    private static final int RFACTOR = 2;

    public ArrayDeque() {
        size = 0;
        //Initialising nextFirst and nextLast to different index at the beginning prevents special case
        nextFirst = 0;
        nextLast = 1;
    }

    @Override
    public void addFirst(T item){
        items[nextFirst] = item;
        ++size;
        --nextFirst;
        if (nextFirst == -1){
            nextFirst = items.length - 1;
        }
        if (size == items.length){
            resize(size * RFACTOR);
        }
    }

    @Override
    public T removeFirst(){
        T removed = null;
        if (size != 0){
            if (getUsageFactor() < 0.25 && size()>= 16){
                shrinkArr();
            }
            ++nextFirst;
            if (nextFirst == items.length){
                nextFirst = 0;
            }
            removed = items[nextFirst];
            --size;
        }
        return removed;
    }

    @Override
    public void addLast(T item){
        items[nextLast] = item;
        ++size;
        ++nextLast;
        if (nextLast == items.length){
            nextLast = 0;
        }
        if (size == items.length){
            resize(size * RFACTOR);
        }
    }

    @Override
    public T removeLast(){
        T removed = null;
        if (size != 0){
            if (getUsageFactor() < 0.25){
                shrinkArr();
            }
            --nextLast;
            if (nextLast == -1){
                nextLast = items.length -1;
            }
            removed = items[nextLast];
            --size;
        }
        return removed;
    }

    private void resize(int capacity){
        T[] new_arr = (T[])new Object[capacity];
        for (int i = 0; i < size; ++i){
            ++nextFirst;
            if (nextFirst == items.length){
                nextFirst = 0;
            }
            new_arr[i] = items[nextFirst];
        }
        items = new_arr;
        nextFirst = items.length - 1;
        nextLast = size;
    }

    private double getUsageFactor(){
        return (double) (size -1)/items.length;
    }

    private void shrinkArr(){
        resize(items.length/2);
    }

    @Override
    public T get(int index){
        T item = null;
        int p = nextFirst;
        if (index <= size-1 && index >= 0){
            for (int i = 0; i <= index; ++i){
                ++p;
                if (p == items.length){
                    p = 0;
                }
                if (i == index){
                    item = items[p];
                }
            }
        }
        return item;
    }

    @Override
    public void printDeque(){
        int p = nextFirst;
        for (int i = 1; i <= size; ++i){
            ++p;
            if (p == items.length){
                p = 0;
            }
            System.out.print(items[p] + " ");
        }
        System.out.println();
    }

    @Override
    public int size(){
        return size;
    }

    @Override
    public Iterator<T> iterator(){
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T>{
        private int pointer;

        public ArrayDequeIterator(){
            pointer = 0;
        }

        @Override
        public boolean hasNext(){
            return pointer < size;
        }

        @Override
        public T next(){
            T returnVal = get(pointer);
            ++pointer;
            return returnVal;
        }
    }

    @Override
    public boolean equals(Object o){
        if (o == this){
            return true;
        }
        else if (o instanceof ArrayDeque){
            ArrayDeque<T> oNew = (ArrayDeque<T>) o;
            if (oNew.size() == this.size()){
                for (int i = 0; i < size; ++i){
                    if (oNew.get(i) != this.get(i)){
                        return false;
                    }
                return true;
                }
            }
        }
        return false;
    }
}