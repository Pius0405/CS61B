/** Class that prints the Collatz sequence starting from a given number.
 *  @author YOUR NAME HERE
 */
public class Collatz {

    /** Gets the next number in a Collatz sequence.
     If n is even, return n/2 or else return 3n +1 */
    public static int nextNumber(int n) {
        if (n % 2 == 0){
            return n / 2;
        }
        return 3*n + 1;
    }

    public static void main(String[] args) {
        int n = 5;
        System.out.print(n + " ");
        while (n != 1) {
            n = nextNumber(n);
            System.out.print(n + " ");
        }
        System.out.println();
    }
}

