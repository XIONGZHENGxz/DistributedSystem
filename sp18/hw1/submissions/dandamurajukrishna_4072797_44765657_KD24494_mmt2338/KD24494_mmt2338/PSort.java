import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public  class PSort {

    public static void parallelSort(int[] A, int begin, int end) {

        int processors = Runtime.getRuntime().availableProcessors();
        ForkJoinPool commonPool = new ForkJoinPool(processors);
        quicksorter task1 = new quicksorter(A , begin, end-1);
        commonPool.invoke(task1);

    }


   private static class quicksorter extends RecursiveTask<Integer>{
        int [] arr;
        int beg;
        int en;

        quicksorter(int[] A,int begin, int end) {
            arr= A;  // possible source of error
            beg=begin;
            en=end;
        }
        @Override
        protected Integer compute() {

            //base case
            if(en-beg <= 16){

                for(int i=beg; i<en+1; i++){

                    for(int j =beg; j<en+1;j++ ){
                        if(arr[i]< arr[j]){

                            int swapval= arr[j];
                            arr[j]= arr[i];
                            arr[i]=swapval;

                        }
                    }

                }




                return 1;

                    /*
                int i, key, j;
                for (i = 1; i < en+1; i++)
                {
                    key = arr[i];
                    j = i-1;


                    while (j >= 0 && arr[j] > key)
                    {
                        arr[j+1] = arr[j];
                        j = j-1;
                    }
                    arr[j+1] = key;
                }

                return 9;
            */

            }

            else if (beg < en  )
            {
			        /* pi is partitioning index, arr[p] is now
			           at right place */
                int pi = this.partition();  // need to figure out partition
                quicksorter left = new quicksorter(arr, beg, pi-1 );    // Before pi
                left.fork();
                quicksorter right = new quicksorter(arr, pi+1 , en);  // After pi

                return right.compute()+left.join();

            }
            else{

                return 1;
            }


        }
        protected int partition(){
            int pivot = arr[en];
            int l = beg-1;
            for(int i =beg; i<= en-1  ; i++ ){
                if(arr[i]<= pivot){
                    l++;
                    int swapvalue1= arr[l];
                    int swapvalue2= arr[i];
                    arr[i]= swapvalue1;
                    arr[l]=swapvalue2;

                }

            }
            int swapvalue1= arr[l+1];
            int swapvalue2= arr[en];
            arr[l+1]=swapvalue2;
            arr[en]= swapvalue1;
            return l+1;


        }



    }




}


