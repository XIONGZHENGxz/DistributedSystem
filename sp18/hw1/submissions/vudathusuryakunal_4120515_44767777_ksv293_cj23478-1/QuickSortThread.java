

import java.util.Arrays;
import java.util.concurrent.RecursiveAction;

public class QuickSortThread extends RecursiveAction{
	int [] A;
	int begin; 
	int end;
	
	public QuickSortThread(int[] a, int begin, int end) {
		this.A = a;
		this.begin = begin;
		this.end = end;
	}

	@Override
	protected void compute() {
		if(end == begin){
			
		}
		else if((end-begin) <= 16){
			insertSort(A, begin, end);
		}
		else{
			int pivotpoint = pivotPartition(A, begin, end - 1);
			QuickSortThread qst1 = new QuickSortThread(A, begin, pivotpoint);
			QuickSortThread qst2 = new QuickSortThread(A, pivotpoint + 1, end);
			qst1.fork();
			qst2.compute();
			qst1.join();
			
		}
		
	}
	
	public int pivotPartition(int[] a, int beg, int e){
		//pivot point
		int midpoint = beg;
		int pivot = a[e];
		for(int i = beg; i < e; i++){
			if(pivot > a[i]){
					//swap
				int s = a[i];
				a[i] = a[midpoint];
				a[midpoint] = s;

				midpoint++;

				
			}
		}

		//swap the pivot to the midpoint
		int s = a[e];
		a[e] = a[midpoint];
		a[midpoint] = s;
		
		return midpoint;
		
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
