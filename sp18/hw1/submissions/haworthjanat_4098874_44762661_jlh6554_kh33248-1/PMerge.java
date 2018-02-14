import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//UT-EID= kh33248, jlh6554

public class PMerge implements Callable<Integer>{
	
	int[] searchArray;
	int[] finalArray;
	int position;
	int elem;
	boolean inA; 
	
	public PMerge(int[] searchArray, int[] finalArray, int position, int elem, boolean inA) {
		this.searchArray = searchArray;
		this.finalArray = finalArray;
		this.position = position;
		this.elem = elem;
		this.inA = inA;
	}
	
	public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads) {
		// TODO: Implement your parallel merge function

		// Each Thread does a binary search on the array to figure out how many
		// entries go before it
		// numThreads is the number of threads that u are given to use
		// ALGO
		// Each thread picks one number and does binary search
		
		ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
		List<PMerge> threads = new ArrayList<PMerge>();
		
		for (int i = 0; i < A.length; i++) {
			//create thread add to pool
			threads.add(new PMerge(B, C, i, A[i], true));
		}
		
		for (int i = 0; i < B.length; i++) {
			threads.add(new PMerge(A, C, i, B[i], false));
		}
		
		try {
			threadPool.invokeAll(threads);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public int binarySearch() {
		int lo = 0;
		int hi = searchArray.length - 1;
		int mid = 0;
		
		while (lo <= hi) {
			mid = (lo + hi) / 2;
			if (this.elem > searchArray[mid]) {
				lo = mid + 1;
			}
			else if (this.elem < searchArray[mid]){
				hi = mid - 1;
			}
			else {
				if (this.inA){
					return mid;
				}
				else {
					return mid + 1; 
				}
			}
		}
		
		return lo;
	}

	@Override
	public Integer call() throws Exception {
		// TODO Auto-generated method stub
		int indx = binarySearch();
		position = position + indx;
		this.finalArray[position] = elem;
		return null;
	}
}
