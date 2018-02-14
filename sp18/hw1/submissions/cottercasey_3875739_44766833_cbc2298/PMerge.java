//UT-EID=cbc2298


import java.util.*;
import java.util.concurrent.*;


public class PMerge implements Runnable{
	
	public static ExecutorService es = Executors.newCachedThreadPool();
	private int A[];
	private int B[];
	private int C[];
	private int threads;
	
	PMerge(int a[], int b[], int c[], int nthreads) {
		this.A = a;
		this.B = b;
		this.C = c;
		threads = nthreads;
	}
	
  public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
	int counterA = 0;
	int counterB = 0;
	int counterC = 0;
    while(counterA < A.length && counterB < B.length){
    	if(A[counterA] <= B[counterB]){
    		C[counterC] = A[counterA];
    		counterA++;
    		counterC++;
    	}
    	else{
    		C[counterC] = B[counterB];
    		counterB++;
    		counterC++;
    	}
    }
    while(counterA < A.length){
    	C[counterC] = A[counterA];
    	counterA++;
    	counterC++;
    }
    while(counterB < B.length){
    	C[counterC] = B[counterB];
    	counterB++;
    	counterC++;
    }

  }
  
  @Override
  public void run() {
	  parallelMerge(A, B, C, threads);
  }
}
