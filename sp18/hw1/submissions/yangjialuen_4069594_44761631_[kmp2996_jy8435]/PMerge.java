//UT-EID= Kevin Pham (kmp2996)
//Ut-EID= Jia-luen Yang (jy8435)

import java.util.*;

public class PMerge implements Runnable {
    
	int[] A;
	int[] B;
	int[] C;
	int b = 0;
	int e = 0;
	
	public PMerge (int[] A, int[]B, int[]C, int begin, int end) {
		this.A = A;
		this.B = B;
		this.C = C;
		this.b = begin;
		this.e = end;
	}
	
	public static int parallelMerge(int[] A, int[] B, int[] C, int numThreads) {
    //arrays A and B are sorted
                    //array C is the merged array
    // your implementation goes here.
	    if(numThreads > (A.length + B.length)) {
	    		numThreads = A.length + B.length;
	    }
	    
        int partSize = (int)Math.ceil((double)(A.length + B.length) / numThreads);
        
		//ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
        
        Thread t[] = new Thread[numThreads];
        
		int begin = 0;
		int end = 0;
		for(int i = 0; i < numThreads; i++) {
			begin = end;
			end += partSize;
			if(end >= A.length + B.length) { end = A.length + B.length; }
			PMerge pm = new PMerge(A, B, C, begin, end);
			t[i] = new Thread(pm);
			t[i].start();
		}
		
        try {
    			for(int i = 0; i < numThreads; i++) {
    				t[i].join(); // wait for element to be sorted   
    			}
        } catch (InterruptedException e) {
        }
		
		return 0;
	}

	public int binarySearch(int x, int[] arr, int low, int high) {
		
		int mid = (low + high)/2;
		
		if (arr.length <= 0) {
			return 0;
		}
		
		if( x == arr[mid]) {
			return mid;
		}
		if (x > arr[high]) {
			return high + 1;
		}
		if( x < arr[low]) {
			return low;
		}
		if (x < arr[mid]) {
			return binarySearch(x, arr, low, mid - 1);
		}
		if (x > arr[mid]) {
			return binarySearch(x, arr, mid + 1, high);
		}
		return -1;
	}
	public int binarySearch2(int x, int[] arr, int low, int high) {
		
		int mid = (low + high)/2;
		
		if (arr.length <= 0) {
			return 0;
		}
		
		if( x == arr[mid]) {
			return mid + 1;
		}
		if (x > arr[high]) {
			return high + 1;
		}
		if( x < arr[low]) {
			return low;
		}
		if (x < arr[mid]) {
			return binarySearch2(x, arr, low, mid - 1);
		}
		if (x > arr[mid]) {
			return binarySearch2(x, arr, mid + 1, high);
		}
		return -1;
	}
	
	@Override
	public void run() {
		for(int i = b; i < e; i++) {
			if(i < A.length) { 
				int index = binarySearch(A[i], B, 0, B.length - 1);
				C[index + i] = A[i]; 
			}
			else {
				int index = binarySearch2(B[i - A.length], A, 0, A.length - 1);
				C[index + i - A.length] = B[i - A.length];
			}
		}
	} 
}
