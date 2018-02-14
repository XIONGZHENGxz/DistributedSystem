//UT-EID = mha664, rob329

import java.util.concurrent.*;

public class PSort{
    public static void parallelSort(int[] A, int begin, int end){

        Sort sort = new Sort(A, begin, end);
        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        pool.invoke(sort);
    }

    private static class Sort extends RecursiveTask<Integer> {

        int[] A;
        int begin;
        int end;

        public Sort(int[] A, int begin, int end){

            this.A = A;
            this.begin = begin;
            this.end = end;
        }

        @Override
        protected Integer compute() {

            if(end - begin <= 16) {
                // sequential insert sort here
                for (int i = begin; i < end; i++) {
                    for(int j = i - 1; j >= begin; j--){
                        if(A[i] < A[j]){
                            int swap = A[j];
                            A[j] = A[i];
                            A[i] = swap;
                            i = j;
                        }
                    }
                }

                return 1;
            }

            int middle = A[((end - begin - 1) / 2) + begin];
            int start = begin;
            int ending = end - 1;

            while(start < ending){

                while(A[start] < middle){
                    start++;
                }

                while(A[ending] > middle){
                    ending--;
                }

                if(start < ending) {
                    int swap = A[start];
                    A[start] = A[ending];
                    A[ending] = swap;
                    start++;
                    ending--;
                }
            }

            Sort s1 = new Sort(A, begin, ending + 1);
            s1.fork();

            Sort s2 = new Sort(A, start, end);

            s2.compute();
            s1.join();

            return 1;
        }
    }
}