//UT-EID=je23237

import java.util.concurrent.*;

public class PSort extends RecursiveAction{
    final int[] A;
    final int begin;
    final int end;

    public PSort(int[] a, int begin, int end) {
        A = a;
        this.begin = begin;
        this.end = end;
    }

    //submit forkjointask to forkjoinpool
	public static void parallelSort(int[] A, int begin, int end){
        int processors = Runtime.getRuntime().availableProcessors();
        PSort psort = new PSort(A,begin,end);
        ForkJoinPool pool = new ForkJoinPool(processors);
        pool.invoke(psort);

  }

    @Override
    protected void compute() {
        //if size <= 16, use seq. insertion sort
        //if begin == end, end program
        if(end-begin <= 16){
            //insertion sort
            int i = begin;
            while(i < end) {
                int j = i;
                while((j > 0) && (A[j-1] > A[j])) {
                    //swap
                    int x = A[j];
                    A[j] = A[j-1];
                    A[j-1] = x;
                    j--;
                }
                i++;
            }
        }
        else{
            //recursive sort
            int mid = (begin + end)/2;
            PSort left = new PSort(A,begin, begin+mid);
            PSort right = new PSort(A,begin+mid, end);
            left.fork();
            right.fork();
            left.join();
            right.join();

        }
    }
}
