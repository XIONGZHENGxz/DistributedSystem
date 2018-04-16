
import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveTask<int[]>{
    private int[] A;
    private int begin;
    private int end;
    public PSort(int[] A, int begin, int end){
        this.A = A;
        this.begin = begin;
        this.end = end;
    }

    @Override
    protected int[] compute() {
        //printArray(A, begin, end);
        if(end - begin <= 16){
            for(int i = begin; i < end; i++) {
                int smallest = A[i];
                int minIndex = i;
                for (int j = i + 1; j < end; j++) {
                    if (A[j] < smallest) {
                        minIndex = j;
                        smallest = A[j];
                    }
                }
                swap(A, i, minIndex);
            }
            return A;
        }
        int pivot = A[begin];
        int i = begin;
        int j = begin + 1;
        for(;j < end; j++){
            if(pivot > A[j]){
                swap(A, i + 1, j);
                i++;
            }
        }
        swap(A, i, begin);
        PSort leftArray = new PSort(A, begin, i);
        leftArray.fork();
        PSort rightArray = new PSort(A, i + 1, end);
        rightArray.compute();
        leftArray.join();
        return A;

    }

    public static void parallelSort(int[] A, int begin, int end){

        int processors = Runtime.getRuntime(). availableProcessors ();
        ForkJoinPool pool = new ForkJoinPool(processors);
        PSort sortArray = new PSort(A, begin, end);
        pool.invoke(sortArray);

    }

    private void swap(int A[], int i, int j){
        int temp = A[i];
        A[i] = A[j];
        A[j] = temp;
     }

     public static void main(String args[]) {
         int size = 10000;
         Random r = new Random();
         int[] test = new int[size];
         int[] test2 = new int[size];
         for (int i = 0; i < test.length; i++) {
             int num = r.nextInt(size);
             test[i] = num;
             test2[i] = num;
         }

         Arrays.sort(test);
         PSort.parallelSort(test2, 0, test2.length);
         System.out.println(Arrays.equals(test, test2));

        /*
        System.out.println("Before Sorting:");
        printArray(test);
        Arrays.sort(test);
        System.out.println("After Sorting:");
        printArray(test);
        System.out.println("--Our Sorting Algorithm--");
        System.out.println("Before Sorting:");
        printArray(test2);
        PSort.parallelSort(test2, 0, test2.length);
        System.out.println("After Sorting:");
        printArray(test2);
        */
     }

    public static void printArray(int[] A) {
        for (int i = 0; i < A.length; i++) {
            if (i != A.length - 1) {
                System.out.print(A[i] + " ");
            } else {
                System.out.print(A[i]);
            }
        }
        System.out.println();
    }

    public static void printArray(int[] A, int begin, int end){
         for(int i = begin; i < end; i++){
             if (i != end - 1) {
                 System.out.print(A[i] + " ");
             } else {
                 System.out.print(A[i]);
             }
        }
        System.out.println();
    }

}
