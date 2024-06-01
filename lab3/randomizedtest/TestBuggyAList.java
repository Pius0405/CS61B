package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import timingtest.AList;

import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    @Test
    public void testThreeAddThreeRemove(){
        AListNoResizing<Integer> al1 = new AListNoResizing<>();
        BuggyAList<Integer> al2 = new BuggyAList<>();
        for (int i = 0; i < 3; i++) {
            al1.addLast(i);
            al2.addLast(i);
        }
        assertEquals(al1.size(), al2.size());
        for (int i = 0; i < 3; ++i){
            int num1 = al1.removeLast();
            int num2 = al2.removeLast();
            assertEquals(num1, num2);
        }
    }

    @Test
    public void randomizedTest(){
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> L_broken = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                L_broken.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int size_L = L.size();
                int size_L_broken = L_broken.size();
                assertEquals(size_L_broken, size_L);
            } else if (operationNumber == 2) {
                if (L.size() != 0) {
                    assertEquals(L.getLast(), L_broken.getLast());
                }
            } else if (operationNumber == 3) {
                if (L.size() != 0) {
                    assertEquals(L_broken.removeLast(), L.removeLast());
                }
            }
        }
    }
}
