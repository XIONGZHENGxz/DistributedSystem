//UT-EID=je23237


import java.util.*;
import java.util.concurrent.*;


public class PMerge{



    public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
        //use Executor Service
        //Split up arrays and look how many lesser values in B and that gives you your end index in C

        int num_threadsA = numThreads/2;
        int num_threadsB = numThreads - numThreads/2;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        for(int i = 1; i <= num_threadsA; i++) {
            //size of chunks
            int chunkA = A.length/num_threadsA;
            //accessing chunk in array
            int chunk_begin = chunkA*(i-1);
            int chunk_end = chunkA*i;

            if(i == num_threadsA) {
                chunkA = A.length / num_threadsA + A.length % num_threadsA;     //if last iteration add remaining element
                chunk_end = Math.min(chunk_begin + chunkA, A.length);           //FIXME might need A.length -1
            }

            process t = new process(chunk_begin,chunk_end,A,B,C, true); //true since my is a
            executor.execute(t);
        }
        for(int i = 1; i <= num_threadsB; i++) {
            //size of chunks
            int chunkB = B.length/num_threadsB;
            //accessing chunk in array
            int chunk_begin = chunkB*(i-1);
            int chunk_end = chunkB*i;

            if(i == num_threadsB) {
                chunkB = B.length / num_threadsB + B.length % num_threadsB;     //if last iteration add remaining element
                chunk_end = Math.min(chunk_begin + chunkB, B.length);           //FIXME might need A.length -1
            }

            process t = new process(chunk_begin,chunk_end,B,A,C, false);
            executor.execute(t);
        }

        executor.shutdown();
        while(!executor.isTerminated()) {

        }


    }

    static public int binarySearch(int key, int[] other, boolean a) {
        int index = Arrays.binarySearch(other, key); //search array for key
        if(index>=0) index++;
        if(index < 0) index = -index;
        return index;
    }
    static class process implements Runnable{

        int chunk_begin, chunk_end;
        int[] my,other, C;
        boolean a;

        public process(int begin, int end, int[] my, int[] other, int[] C,boolean a) {
            this.chunk_begin = begin;
            this.chunk_end = end;
            this.my = my;
            this.other = other;
            this.C = C;
            this.a = a;
        }


        @Override
        public void run() {
            for(int i = chunk_begin; i < chunk_end; i++){
                int index = binarySearch(my[i],other,a);
                index = index + i - 1; // add index after binary search to current index
                add(index,my[i],C);
            }

        }
    }
    static synchronized void add(int index, int val, int[] C){
        if (C[index] == val){
            C[index + 1] = val;
        }else{
            C[index] = val;
        }
    }
}