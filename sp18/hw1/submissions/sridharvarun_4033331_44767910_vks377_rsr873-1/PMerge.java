//UT-EID= vks377, rsr873


import java.util.*;
import java.util.concurrent.*;


public class PMerge implements Callable<int[]>{
	final int[] A;
	final int[] B;
	int[] C;
	int start, end;
	boolean arrayA;
	static Set<Integer> numbers = new HashSet<Integer>();
	
	public PMerge(int[] A, int[] B, int[] C, int start, int end, boolean arrayA) {
		this.A = A;
		this.B = B;
		this.C = C;
		this.start = start;
		this.end = end;
		this.arrayA = arrayA;
	}
	
	public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads){
		// TODO: Implement your parallel merge function
		try {
			ExecutorService es = Executors.newFixedThreadPool(numThreads);
			int elemPerTaskA = A.length / (numThreads / 2);
			for(int i = 0; i < A.length; i+=elemPerTaskA) {
				PMerge p;
				if(i + (elemPerTaskA - 1) > A.length - 1)
					p = new PMerge(A, B, C, i, A.length - 1, true);
				else
					p = new PMerge(A, B, C, i, i + (elemPerTaskA - 1), true);
				Future<int[]> f = es.submit(p);
				f.get();
			}
			int elemPerTaskB = B.length / (numThreads / 2);
			for(int i = 0; i < B.length; i+=elemPerTaskB) {
				PMerge p;
				if(i + (elemPerTaskB - 1) > B.length - 1)
					p = new PMerge(A, B, C, i, B.length - 1, false);
				else
					p = new PMerge(A, B, C, i, i+(elemPerTaskB - 1), false);
				Future<int[]> f = es.submit(p);
				f.get();
			}
			//System.out.println("Answer is " + Arrays.toString(C));
			es.shutdown ();
		} catch (Exception e) { System.err.println (e); }
	}
	
	public int[] call() {
		if(arrayA) {
			for(int i = start; i <= end; i++) {
				int BIndex = binarySearch(B, A[i]);
				C[i + BIndex] = A[i];
			}
		}
		else {
			for(int i = start; i <= end; i++) {
				int AIndex = binarySearch(A, B[i]);
				C[i + AIndex] = B[i];
			}
		}
		return C;
	}
	
	private int binarySearch(int[] array, int num) {
		int start = 0;
		int end = array.length - 1;
		while(start <= end) {					
			int mid = start + (end - start)/2;
			if(array[mid] == num) { 
				if(numbers.contains(num))
					return mid + 1;
				numbers.add(num);
				return mid;
			}
			else if(num < array[mid]) 
				end = mid - 1;
			else
				start = mid + 1;
		}
		numbers.add(num);
		return start;
		// add indexA and start/end (always choose start)
	}
}
