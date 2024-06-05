package deque;
import deque.MaxArrayDeque;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Comparator;


public class MaxArrayDequeTest {
    @Test
    public void testMax() {
        MaxArrayDeque<String> testDeque = new MaxArrayDeque<>(new GenericStringComparator<>());
        testDeque.addFirst("Hello");
        testDeque.addFirst("World");
        testDeque.addLast("JavaIntellij");
        testDeque.addLast("Ok");
        assertEquals("World", testDeque.max());
    }
}

class GenericStringComparator<T extends String> implements Comparator<T> {
    @Override
    public int compare(T s1, T s2) {
        return s1.compareTo(s2);
    }
}



