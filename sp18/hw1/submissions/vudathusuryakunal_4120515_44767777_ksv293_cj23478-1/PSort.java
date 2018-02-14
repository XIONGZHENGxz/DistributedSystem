

//UT-EID=


import java.util.*;
import java.util.concurrent.*;

public class PSort{
  public static void parallelSort(int[] A, int begin, int end){
  
	  if(begin == end){
		  A = insertSort(A,begin,end);
	  }
	  else if(A.length <= 16){
		  insertSort(A, begin, end);
	  }
	  else{
		  
		  ForkJoinPool fjp = new ForkJoinPool();
		  QuickSortThread Qst = new QuickSortThread(A,begin,end);
		  fjp.invoke(Qst);
		 
	  }
	
	 
  }
  
  public static int[] insertSort (int[] A, int begin, int end){
	    int j, val, idx = 0;
	    for(int i = begin; i < end; i++){
	      val = A[i];
	      j = i-1;
	      while (j >= begin && A[j] > val){
	        A[j+1] = A[j];
	        j--;
	      }
	      A[j+1] = val;
	    }
	  
	  return A;
	}
}
