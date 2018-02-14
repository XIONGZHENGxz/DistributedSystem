import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class PMergeTask implements Callable<Object> {
	Integer[] subArrayA;
	Integer[] subArrayB;
	int[] A;
	int[] B;
	int[] C;
	int numThreads;
	int foo;
	
	PMergeTask(Integer[] apple, Integer[] bell, int[] A, int[] B, int[] C, int numThreads, int i) {
		this.subArrayA = apple;
		this.subArrayB = bell;
		this.A = A;
		this.B = B;
		this.C = C;
		this.numThreads = numThreads;
		this.foo = i;
	}

	@Override
	public Object call() throws Exception {
		// starting from the first element of the subArray of A (let's call it traverse), use binary search to find
		// the first element in array B that is larger (let's call it X)
		
		// all the elements before X in array B will be put in array C
		// then traverse will be put in array C
		
		// we repeat this process until we finish all the elements in chunk
		
		int wow = foo*(A.length/numThreads);
		for(int i = 0; i < subArrayA.length; i++) {
			int here = binarySearchNow(B, subArrayA[i]);
			C[here+i+wow] = subArrayA[i];
		}
		int omg = foo*(B.length/numThreads);
		for(int k = 0; k < subArrayB.length; k++) {
			int place = binarySearchNow(A, subArrayB[k]);
			C[place+k+omg] = subArrayB[k];
		}
		return null;
	}
	
	public int binarySearchNow(int[] ref, int target) {
        int l = 0, r = ref.length - 1;
        while (l <= r) {
            int m = l + (r-l)/2;
            if (ref[m] == target) return m;			// target is found
            if (ref[m] < target) l = m + 1; 		// target is larger
            else r = m - 1; 						// target is smaller
        }
        return l;									// target is NOT found
    }
}
