//UT-EID= arg3532


import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveTask<Void>{
    int[] toSort;
    int begin;
    int end;

    public PSort(int[] A, int begin, int end){
        this.toSort = A;
        this.begin = begin;
        this.end = end;
    }

    public static void main(String[] args){
        int[] A = {3, 5, 1, 8, 6, 9, 7};
        parallelSort(A, 0, A.length);
        System.out.println(Arrays.toString(A));
    }

    public static void parallelSort(int[] A, int begin, int end){
        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        PSort p = new PSort(A, begin, end-1);
        pool.invoke(p);
    }

    @Override
    protected Void compute() {
        //System.out.println("New thread - Begin: " + begin + " End: " + end);
        if((end - begin) < 16){
            insertionSort();
            //System.out.println("End thread - Begin: " + begin + " End: " + end);
        }
        else {
            if (begin < end) {
                int partitionIndex = quickSortPartition();
                //System.out.println("End thread - Begin: " + begin + " End: " + end);
                PSort p1 = new PSort(toSort, begin, partitionIndex - 1);
                p1.fork();
                PSort p2 = new PSort(toSort, partitionIndex + 1, end);
                p2.compute();
                p1.join();
            }
        }
        return null;
    }

    private int quickSortPartition(){
        int pivot = toSort[end];
        int i = (begin - 1);
        for(int j = begin; j < end; j++){
            if(toSort[j] <= pivot){
                i++;
                int temp = toSort[i];
                toSort[i] = toSort[j];
                toSort[j] = temp;
            }
        }

        int temp = toSort[i + 1];
        toSort[i + 1] = toSort[end];
        toSort[end] = temp;
        return i + 1;
    }

    private void insertionSort(){
        int valToMove = 0;
        int j = 0;
        for(int i = begin + 1; i <= end; i++){
            valToMove = toSort[i];
            j = i - 1;
            while(j >= begin && toSort[j] > valToMove){
                toSort[j+1] = toSort[j];
                j--;
            }
            toSort[j+1] = valToMove;
        }
    }
}