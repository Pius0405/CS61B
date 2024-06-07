package flik;
import static flik.Flik.isSameNumber;
import static org.junit.Assert.*;
import org.junit.Test;


public class TestFlik {
    @Test
    public void testIsSameNumber() {
        int y = 0;
       for (int x = 0; x < 500; x++, y++) {
           assertTrue(isSameNumber(x, y));
       }
    }
}

/*
The bug is at Flik due to the use of == to check if the integers are the same instead of .equals()
As integers (type int) are passed in, they are converted to Integer objects (type Integer) --> Autoboxing
Java has an Integer caching mechanism that stores Integer objects after they are created and can be reused
**For integers in range -128 to 127 only
Thus, from 0 to 127 there's no error since == returns true for the same object
When it comes to 128, different Integer objects were created causing 128 to be printed instead of 500
**/