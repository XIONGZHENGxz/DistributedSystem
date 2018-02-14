import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class PSortTest {

    private static Random rand = new Random();

    private int[] randomArray(int length) {
        int[] newArray = new int[length];

        for (int i = 0; i < length; i++) {
            newArray[i] = rand.nextInt(1000);
        }

        return newArray;
    }

    @Test
    void generalTest1() {
        int[] x = {-5, 2, 3, -1, 0};
        PSort.parallelSort(x, 0, 5);
        assertArrayEquals(new int[] {-5, -1, 0, 2, 3}, x);
    }

    @Test
    void generalTest2() {
        int[] x = {2599666, 1000000, 3000000, 9000000};
        PSort.parallelSort(x, 0, 2);
        assertArrayEquals(new int[] {1000000, 2599666, 3000000, 9000000}, x);
    }

    @Test
    void indexTest1() {
        int[] x = {-23, -59, 49, 0, 2, -5, 8, 19};
        PSort.parallelSort(x, 4, 7);
        assertArrayEquals(new int[] {-23, -59, 49, 0, -5, 2, 8, 19}, x);
    }

    @Test
    void parTest1() {
        int[] x = randomArray(1000);
        int[] expected = Arrays.copyOf(x, x.length);
        Arrays.sort(expected);
        PSort.parallelSort(x, 0, x.length);
        assertArrayEquals(expected, x);
    }

    @Test
    void parTest2() {
        int[] x = randomArray(5000);
        int[] expected = Arrays.copyOf(x, x.length);
        Arrays.sort(expected);
        PSort.parallelSort(x, 0, x.length);
        assertArrayEquals(expected, x);
    }

    @Test
    void parTest3() {
        int[] x = randomArray(10000);
        int[] expected = Arrays.copyOf(x, x.length);
        Arrays.sort(expected);
        PSort.parallelSort(x, 0, x.length);
        assertArrayEquals(expected, x);
    }

    @Test
    void boundaryTests() {
        int[] x = {5, -2};
        PSort.parallelSort(x, 0, 2);
        assertArrayEquals(new int[] {-2, 5}, x);

        int[] x1 = {3};
        PSort.parallelSort(x1, 0, 1);
        assertArrayEquals(new int[] {3}, x1);
    }

}

