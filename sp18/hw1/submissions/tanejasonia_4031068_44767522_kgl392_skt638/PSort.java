//UT-EID= kgl392, skt638


import java.util.*;
import java.util.concurrent.*;

public class PSort{
    public static void parallelSort(int[] A, int begin, int end){

        int processors = Runtime.getRuntime().availableProcessors();
        QSort sort = new QSort(A, begin, end);
        ForkJoinPool pool = new ForkJoinPool(processors);
        pool.invoke(sort);

    }

    public static class QSort extends RecursiveTask<Integer>{
        public int[] a;
        public int begin;
        public int end;

        public QSort(int[] a, int begin, int end){
            this.a = a;
            this.begin = begin;
            this.end = end;
        }

        protected Integer compute() {
            // if less than or equal 16 run insertion sort and end
            if(((end - begin) <= 16) && (begin < end)){
                insertion_sort(a, begin, end);
                end = begin;
               return 0;
            }
            // if greater than 16 run quicksort
            if((end - begin) > 16 && begin < end){
                int parti = partition(a, begin, end);
                QSort q1 = new QSort(a, begin, parti);
                q1.fork();
                QSort q2 = new QSort(a, parti + 1, end);
                return q2.compute() + q1.join();
            }
            return 0;
        }

        // insertion sort
        public static int insertion_sort(int[]array, int begin, int end){
            int insert_value;
            int index;
            for(int i = begin; i < end; i++){
                insert_value = array[i];
                index = i-1;

                while(index >= 0 && array[index] > insert_value){
                    array[index+1] = array[index];
                    index = index -1;
                }
                array[index+1] = insert_value;
            }
            return 0;
        }

        // partition method to sort
        public static int partition(int[] array, int begin, int end){
            int pivot = array[end-1];
            int j = begin-1;
            for(int i = begin; i < end - 1; i++){
                if(array[i] <= pivot){
                    j++;
                    int temp = array[i];
                    array[i] = array[j];
                    array[j] = temp;
                }
            }
            array[end-1] = array[j+1];
            array[j+1] = pivot;
            return (j+1);

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
    }


}

