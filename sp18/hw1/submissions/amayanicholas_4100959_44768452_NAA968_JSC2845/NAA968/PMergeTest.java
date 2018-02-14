import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PMergeTest {

    @Test
    void boundTest1() {
        int[] a = {1};
        int[] b = {};
        int[] c = new int[a.length + b.length];
        int[] expected = {1};
        PMerge.parallelMerge(a, b, c, 4);
        assertArrayEquals(expected, c);
    }

    @Test
    void boundTest2() {
        int[] a1 = {};
        int[] b1 = {3};
        int[] c1 = new int[a1.length + b1.length];
        int[] expected1 = {3};
        PMerge.parallelMerge(a1, b1, c1, 4);
        assertArrayEquals(expected1, c1);
    }

    @Test
    void boundTest3() {
        int[] a2 = {};
        int[] b2 = {3};
        int[] c2 = new int[a2.length + b2.length];
        int[] expected2 = {3};
        PMerge.parallelMerge(a2, b2, c2, 1);
        assertArrayEquals(expected2, c2);
    }

    @Test
    void boundTest4() {
        int[] a = {};
        int[] b = {};
        int[] c = new int[a.length + b.length];
        int[] expected2 = {};
        PMerge.parallelMerge(a, b, c, 1);
        assertArrayEquals(expected2, c);
    }

    @Test
    void genTest1() {
        int[] a = {-2, 0, 1, 5, 6};
        int[] b = {-1, 2, 7};
        int[] c = new int[a.length + b.length];
        int[] expected = {-2, -1, 0, 1, 2, 5, 6, 7};
        PMerge.parallelMerge(a, b, c, 4);
        assertArrayEquals(expected, c);
    }

    @Test
    void genTest2() {
        int[] a = {-17, 2, 9, 19, 21};
        int[] b = {-8, 3, 4, 7, 20, 35, 45, 55, 900};
        int[] c = new int[a.length + b.length];
        int[] expected = {-17, -8, 2, 3, 4, 7, 9, 19, 20, 21, 35, 45, 55, 900};
        PMerge.parallelMerge(a, b, c, 4);
        assertArrayEquals(expected, c);
    }

//    @Test
//    void binSearchEvenTest() {
//        int[] x = {3, 11, 18, 23};
//        int i = 17;
//        int result = PMerge.binarySearch(i, x, 0, 3);
//        assertEquals(2, result);
//    }
//
//    @Test
//    void binSearchOddTest() {
//        int[] x = {3, 11, 18, 23, 35};
//        int i = 21;
//        int result = PMerge.binarySearch(i, x,0, 4);
//        assertEquals(3, result);
//    }
//
//    @Test
//    void binSearchLastElementTest() {
//        int[] x = {3, 11, 18, 23};
//        int i = 30;
//        int result = PMerge.binarySearch(i, x, 0, 3);
//        assertEquals(4, result);
//    }
//
//    @Test
//    void binSearchFirstElementTest() {
//        int[] x = {3, 11, 18, 23};
//        int i = 1;
//        int result = PMerge.binarySearch(i, x, 0, 3);
//        assertEquals(0, result);
//    }
}
