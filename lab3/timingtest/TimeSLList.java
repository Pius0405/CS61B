package timingtest;
import edu.princeton.cs.algs4.Stopwatch;
import org.checkerframework.checker.units.qual.A;
import sun.tools.tree.DoubleExpression;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
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

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        //constant number of operations
        int OPS = 10000;
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();
        SLList<Integer> test_lst;
        for (int n = 1000; n <= 128000; n *= 2){
            //Create an SLList
            test_lst = new SLList<>();
            //Add N items to the SLList
            for (int i = 0; i <= n; ++i){
                test_lst.addLast(0);
            }
            Stopwatch sw = new Stopwatch();
            for (int j = 1; j <= OPS; j++){
                test_lst.getLast();
            }
            Double time_in_sec = sw.elapsedTime();
            Ns.addLast(n);
            times.addLast(time_in_sec);
            opCounts.addLast(OPS);
        }
        printTimingTable(Ns, times, opCounts);
    }

}
