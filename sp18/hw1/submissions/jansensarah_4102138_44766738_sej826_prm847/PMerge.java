//UT-EID=


import java.util.*;
import java.util.concurrent.*;


public class PMerge{
  public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads){
    // TODO: Implement your parallel merge function
	  int numOfElem = C.length;
	  
	  Thread[] threads = new Thread[numThreads];
	  
	  for(int i = 0; i < numThreads - 1; i++){
		  threads[i] = new Thread(new PMerge().new MergePartial(A, B, C, i*numOfElem/numThreads, (i+1)*numOfElem/numThreads));
		  threads[i].start();
	  }
	  threads[numThreads - 1] = new Thread(new PMerge().new MergePartial(A, B, C, (numThreads - 1)*numOfElem/numThreads, C.length));
	  
	  threads[numThreads - 1].start();
	  
	  for(Thread thread: threads){
		  try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
  }
  
  class MergePartial implements Runnable{
	  int[] A, B, C;
	  int begin, end;
	  MergePartial(int[] A, int[] B, int[] C, int begin, int end){
		  this.A = A;
		  this.B = B;
		  this.C = C;
		  this.begin = begin;
		  this.end = end;
	  }
	  
	  @Override
	  public void run() {
		  for(int i = begin; i < end; i++){
			  if(i<A.length){
				  //find amount strictly less than
				  int lessThanAinB = binSearch(B, A[i], 0, B.length - 1);
				  if(lessThanAinB >= 0 && A[i] <= B[lessThanAinB]){
					  lessThanAinB--;
				  }
				  C[i + lessThanAinB + 1] = A[i];
			  }
			  else{
				  //find amount greater than are equal to
				  int lessThanBinA = binSearch(A, B[i - A.length], 0, A.length - 1);
				  if(lessThanBinA >= 0 && B[i - A.length] < A[lessThanBinA]){
					  lessThanBinA--;
				  }
				  C[i - A.length + lessThanBinA + 1] = B[i - A.length];
			  }
		  }
	  }
	  
	  private int binSearch(int[] A, int elem, int first, int last){
		  if(first >= last){
			  return last;
		  }
		  int middle = (first+last)/2;
		  if(A[middle] < elem){
			  return binSearch(A, elem, middle+1, last);
		  }
		  if(A[middle] > elem){
			  return binSearch(A, elem, first, middle-1);
		  }
		return middle;
	  }
	  
  }
}
