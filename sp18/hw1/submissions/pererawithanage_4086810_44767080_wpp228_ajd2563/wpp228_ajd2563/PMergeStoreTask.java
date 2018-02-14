
import java.util.concurrent.Callable;


public class PMergeStoreTask implements Callable<Boolean> {

    int n, i, offset;
    int[] A, result;
    
    public PMergeStoreTask(int n, int i, int[] a, int[] result, int offset) {
        this.n = n;
        this.i = i;
        this.A = a;
        this.result = result;
        this.offset = offset;
    }

    private int numElementsBelow(int[] arr, int n) {

        for (int i=0; i<arr.length; i++) {
            if (arr[i] > n) {
                return i;
            } else if (arr[i] == n) {
                return i + offset;
            }
        }

        return arr.length;
    }

    public Boolean call() {
        int idx = i + numElementsBelow(A, n);
        result[idx] = n;

        return true;
    }
}
