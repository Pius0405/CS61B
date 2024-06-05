package deque;

import org.junit.Test;

import static deque.TimeArrayDeque.timeArrayDequeConstruction;
import static org.junit.Assert.*;
import edu.princeton.cs.algs4.StdRandom;
import edu.princeton.cs.algs4.Stopwatch;


public class ArrayDequeTest {
    /*
    Test for the following ArrayDeque methods:
    1. addFirst
    2. addLast
    3. get
    4. removeFirst
    5. removeLast
    6. size
    7. isEmpty
    */
    @Test
    public void randomizedTest(){
        int N = 10000;
        int result;
        for (int i = 0; i < N; i++){
            int operationNumber = StdRandom.uniform(1,7);
            ArrayDeque<Integer> testDeque = new ArrayDeque<>();
            switch (operationNumber){
                case 1:
                    testDeque.addFirst(1);
                    result = testDeque.get(0);
                    assertEquals(1,result);
                    break;
                case 2:
                    testDeque.addLast(1);
                    result = testDeque.get(0);
                    assertEquals(1,result);
                    break;
                case 3:
                    testDeque.addFirst(2);
                    result = testDeque.removeFirst();
                    assertEquals(2,result);
                    break;
                case 4:
                    testDeque.addLast(3);
                    result = testDeque.removeLast();
                    assertEquals(3,result);
                    break;
                case 5:
                    testDeque.addLast(4);
                    testDeque.addFirst(5);
                    result = testDeque.size();
                    assertEquals(2,result);
                    break;
                case 6:
                    assertTrue(testDeque.isEmpty());
                    testDeque.addLast(5);
                    testDeque.addFirst(6);
                    assertFalse(testDeque.isEmpty());
                    break;
            }
        }
    }

    @Test
    public void randomizedTest2(){
        int N = 10000;
        ArrayDeque<Integer> testDeque = new ArrayDeque<>();
        for (int i = 0; i < N; i++){
            testDeque.addFirst(i);
            testDeque.addLast(i);
        }
    }
}

class TimeArrayDeque {
    private static void printTimingTable(ArrayDeque<Integer> Ns, ArrayDeque<Double> times, ArrayDeque<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void timeArrayDequeConstruction() {
        ArrayDeque<Integer> Ns = new ArrayDeque<>();
        ArrayDeque<Double> times = new ArrayDeque<>();
        ArrayDeque<Integer> opCounts = new ArrayDeque<>();
        ArrayDeque<Integer> test_lst;
        for (int i = 1000; i <= 128000; i *= 2) {
            Ns.addLast(i);
            test_lst = new ArrayDeque<>();
            Stopwatch sw = new Stopwatch();
            for (int j = 1; j <= i; ++j) {
                test_lst.addLast(0);
            }
            Double time_in_sec = sw.elapsedTime();
            times.addLast(time_in_sec);
            opCounts.addLast(i);
        }
        printTimingTable(Ns, times, opCounts);
    }
}


