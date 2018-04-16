import java.util.Arrays;
import java.util.concurrent.Callable;

public class Merger implements Callable {
    boolean first;
    int a[];
    int b[];
    int c[];
    int start;
    int end;

    public Merger() {
    }

    public Merger(int start, int end, int[] a, int[] b, int[] c, boolean first) {
        this.start = start;
        this.end = end;
        this.a = a;
        this.b = b;
        this.c = c;
        this.first = first;
    }

    public static void main(String[] args) {
        int[] a = { 1, 3, 5 };
        int[] b = { 1, 2, 4, 6 };
        Merger m = new Merger();
    }

    @Override
    public Boolean call() {
        for (int k = start; k < end; k++) {
            int offset = Arrays.binarySearch(b, a[k]);
            if (offset >= 0) {
                if (first) offset += 1;
            }
            else offset = Math.abs(offset) - 1;
            int newIndex = k + offset;
            c[newIndex] = a[k];
        }
        return true;
    }
}
