//UT-EID= jro769 mac7865


import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveTask <Boolean>{
	final int[] A;
	final int begin;
	final int end; 
	
	PSort(int[] A, int begin, int end){
		this.A = A;
		this.begin = begin;
		this.end = end;
	}
	
	@Override
	protected Boolean compute() {
		if(begin == end){
			  return true;
		  }
		  else if(end-begin <= 16){
			  return insertSort(A,begin,end);
		  }
		  else{
			  int half = (end-begin)/2;
			  PSort first_half = new PSort(A,begin,begin+half);
			  first_half.fork(); 
			  PSort second_half = new PSort(A,begin+half,end); 
			  return second_half.compute() && first_half.join(); 
		  }
	}

	private static Boolean insertSort(int[] A, int begin, int end){
		for(int i = begin; i < end; i ++){
			for (int j = i; j> begin; j--){
				if(A[j] < A[j-1]){
					int temp = A[j];
					A[j] = A[j-1];
					A[j-1] = temp; 
				}
			}
		}
		return true; 
	}
  
	public static void parallelSort(int[] A, int begin, int end){
	  
		int processors = Runtime.getRuntime().availableProcessors();
		//System.out.println("Number of processors : "+ processors );
		PSort f = new PSort (A,begin,end);
		ForkJoinPool pool = new ForkJoinPool(processors);
		Boolean result = pool.invoke(f);
		//System.out.println ("Result : " + result );
		//System.out.println(A.toString());
		}

	
}
