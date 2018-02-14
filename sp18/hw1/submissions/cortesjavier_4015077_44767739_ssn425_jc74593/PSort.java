//UT-EID= ssn425, jc74593

import java.util.concurrent.*;

public class PSort extends RecursiveAction {
	private static final long serialVersionUID = 1L;
	int[] a;
	int begin, end;
	
	public PSort(int[] a, int begin, int end)
	{
		this.a = a; 
		this.begin = begin;
		this.end = end;
	}
	
	protected void compute()
	{
		if(begin == end){
			return;
		}
		
		if(end - begin <= 16){
			for(int i = begin+1; i < end; i++){
				int val = a[i];
				int j = i - 1;
				while(j>=begin && val < a[j]){
					swap(a, j, j+1);
					j--;
				}
			}
			
			return;
		}
		
		if(begin < end){
			int p = partition(a, begin, end);
			
			PSort sort0 = new PSort(a, begin, p);
			PSort sort1 = new PSort(a, p+1, end);
			sort0.fork();
			sort1.compute();
			sort0.join();
			return;
		}
	}
	
	private int partition(int[] a, int begin, int end) {
		int pivot = a[end - 1];
		int i = begin - 1;
		
		for(int j = begin; j < end-1; j++){
			if(a[j] <= pivot){
				i++;
				swap(a, i, j);
			}
		}
		
		swap(a, i+1, end-1);
		return i+1;
	}

	public static void parallelSort(int[] a, int begin, int end){
	    int processors = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processors);
		PSort p = new PSort(a, begin, end);
		pool.invoke(p);
	}
	
	public static void swap(int[] a, int d1, int d2)
	{
		int temp;
		temp = a[d1];
		a[d1] = a[d2];
		a[d2] = temp;
	}
}