//UT-EID= ASM2992 & DEL824


import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveTask<Void>{
	private int[] A;
	private int begin, end;
	
  public static void parallelSort(int[] A, int begin, int end){
	  int processors = Runtime.getRuntime().availableProcessors();
	  PSort p = new PSort(A,begin,end);
	  ForkJoinPool pool=new ForkJoinPool(processors);
	  pool.invoke(p);
  }


	private PSort(int[] A, int begin, int end) {
		this.A = A;
		this.begin = begin;
		this.end = end;
	}
	
protected Void compute() {
	if(A==null||A.length==0){return null;}
	//System.out.println(Thread.currentThread().getName());
	if((end-begin)<=16){
		int temp;
        for (int i = begin; i < end; i++) {
            for(int j = i ; j > 0 ; j--){
                if(A[j] < A[j-1]){
                    temp = A[j];
                    A[j] = A[j-1];
                    A[j-1] = temp;
                }
            }
        }
        return null;
	}
	else{
		int pivot = A[begin+((end-begin)/2)];
		int i = begin;
		int j = end - 1;
		
		while(i <= j) {
			while(A[i] < pivot) {
				i++;
			}
			while(A[j] > pivot) {
				j--;
			}
			if(i <= j) {
				int temp = A[i];
				A[i] = A[j];
				A[j] = temp;
				i++;
				j--;
			}
		}
		
		
		PSort ps1 = new PSort(A, begin, i);
		ps1.fork();
		ps1.compute();
		PSort ps2 = new PSort(A, i, end);
		ps2.compute();
		ps1.join();
		return null;
	}
}
}