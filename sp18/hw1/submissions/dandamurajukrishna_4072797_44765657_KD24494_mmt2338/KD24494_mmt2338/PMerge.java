import java.util.TreeMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class PMerge {

    public static int parallelMerge(int[] A, int[] B, int[] C, int numThreads) {
        ForkJoinPool current = new ForkJoinPool(numThreads);

        for (int i =0; i<A.length;i++){  // use WithB
            merger current2 = new merger(B, C, A[i],  i);
            current.invoke(current2);

        }


        for(int  i=0; i< B.length ;i++){ // use withA
            merger current1 = new merger( A,  C, B[i],  i );
            current.invoke(current1);
        }






        return 1;
    }

    public static class merger extends RecursiveTask<Integer> {
        int [] A;
        int [] C;
        int value;
        int index;

        @Override
        protected Integer compute() {  // Do Binary Search

            int index1 =0;
            int flag =0;
            if(value < A[0]){
                C[index]= value; // 0+index
                return  1;
            }

           else if(value >A[A.length-1]){
                C[A.length+index]= value; // i + index gives the value
                return  1;

            }

            for(int i =1; i<A.length  && flag ==0 ;i++){
                if(value<A[i]){
                    index1= i; // previos index
                    flag= 1; // end

                }

            }

            C[index1+index]= value; // i + index gives the value
            return  1;

        }

        protected merger(int [] A, int[] C,int value, int index  ){
        this.A =A;
        this.C=C;
        this.value =value;
        this.index =index;
        }


     }
}