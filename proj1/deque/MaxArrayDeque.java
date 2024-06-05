package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T>{
    private Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> c){
        super();
        comparator = c;
    }

    public T max(){
        T max = null;
        if (! isEmpty()){
            max = get(0);
            T current;
            for (int i=1; i<size(); i++){
                current = get(i);
                if (comparator.compare(current, max) > 0){
                    max = current;
                }
            }
        }
        return max;
    }

    public T max (Comparator<T> c){
        T max = null;
        if (! isEmpty()){
            max = get(0);
            T current;
            for (int i=1; i<size(); i++){
                current = get(i);
                if (c.compare(current, max) > 0){
                    max = current;
                }
            }
        }
        return max;
    }
}
