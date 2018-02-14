//UT-EID=
//Shamma Kabir = sk38422
//George Doykan = gd7448


import java.util.*;
import java.util.concurrent.*;


public class PMerge{
  public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads){
	  ExecutorService pool = Executors.newFixedThreadPool(numThreads);
	  merge[] mergeArr = new merge[numThreads];
	 	  
	  int chunkA = (A.length + numThreads - 1)/numThreads;
	  int chunkB = (B.length + numThreads - 1)/numThreads;
	  //System.out.println("Chunk Size A: " + chunkA + " Chunk Size B: " + chunkB);
	  int sA; int sB; int eA; int eB; int prevA = 0; int prevB = 0;
	  for (int t = 0; t < numThreads; t++) {
		  //sA = calcStart(t, chunkA);
		  //sB = calcStart(t, chunkB);
		  sA = prevA;
		  sB = prevB;
		  eA = calcEnd(t, sA, chunkA, A.length, numThreads);
		  prevA = eA;
		  eB = calcEnd(t, sB, chunkB, B.length, numThreads);
		  prevB = eB;
		  //eA = Math.min(sA+chunkA, A.length);
		  //eB = Math.min(sB+chunkB, B.length);
		  //System.out.println(sA + " " + eA + " " + sB + " " + eB + " ");
		  mergeArr[t] = new merge(sA, eA, sB, eB, A, B, C);
		 // pool.submit(mergeArr[t]);
		  Future<?> f =  pool.submit(mergeArr[t]);
		  try {
			f.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	  pool.shutdown();
  }
  
 /* static int calcStart(int t, int chunkSize) {
	  return t*chunkSize;
  }*/
  
  static int calcEnd(int t, int start, int chunkSize, int length, int numThreads) {
	  int endVal;
	  if (length - start == numThreads - t) {
		  if (start + 1 <= length)
			  endVal = start + 1;
		  else
			  endVal = start;
	  }
	  else {
		  endVal = Math.min(start+chunkSize, length);
	  }
	  return endVal;
  }
  
  public static void main(String[] args) {
	    int[] x = {1, 2, 3, 5, 10, 21};
	    int[] y = {1, 4, 7};
	    int[] z = new int[9];
	    //System.out.println("this is x : " + Arrays.toString(x));
	   // System.out.println("this is y : " + Arrays.toString(y));
	    parallelMerge(x, y, z, 4);
	    //System.out.println("this is z : " + Arrays.toString(z));
	  }
}


class merge implements Runnable {
	int sA; 
	int eA;
	int sB; 
	int eB;
	int[] a; 
	int [] b; 
	int [] c; 
	public merge(int sA, int eA, int sB, int eB, int[] a, int [] b, int [] c) {
		this.sA = sA; 
		this.sB = sB;
		this.eA = eA; 
		this.eB = eB;
		this.a = a; 
		this.b = b;
		this.c = c;
	}
	

	@Override
	public void run() {
	    // TODO Auto-generated method stub
	    //go through A
	    for (int i=sA; i <eA; i++) {
	      int insertionIndex = Arrays.binarySearch(b, a[i]);
	      //calculating the index of the element in C
	      if (insertionIndex < 0) {
	          insertionIndex = -insertionIndex-1 + i;
	      }else {
	          insertionIndex = insertionIndex + i + 1; //insert one index ahead
	      }
	      //System.out.println("inserting from array A " + a[i] + " insertion index in c is : " + insertionIndex);
	      //insert element in C
	      c[insertionIndex] = a[i];
	    }
		

	    //go through B
	    for (int i=sB; i <eB; i++) {
	      int insertionIndex = Arrays.binarySearch(a, b[i]);
	      //calculating the index of the element in C
	      if (insertionIndex < 0) {
	        insertionIndex = -insertionIndex-1 + i;
	    	  
	      }else{
	        insertionIndex += i;
	      }

	      //insert element in C
	      //System.out.println("inserting from array B " + b[i] + " insertion index in c is : " + insertionIndex);
	      c[insertionIndex] = b[i];
	    }
		

	  }
	
}