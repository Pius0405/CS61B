package deque;

public class ArrayDeque<T>{
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

    public T removeFirst(){
        T removed = null;
        if (size != 0){
            if (getUsageFactor() < 0.25){
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

    public void resize(int capacity){
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

    public double getUsageFactor(){
        return (double) (size -1)/items.length;
    }

    public void shrinkArr(){
        resize(items.length/2);
    }


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

    public int size(){
        return size;
    }
    public boolean isEmpty(){
        return size == 0;
    }
}