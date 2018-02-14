//UT-EID=mh43862; ksm2464


import java.util.*;
import java.util.concurrent.*;


public class PMerge{

  	static int ind;
  	static int[] my_A;
  	static int[] my_B;
  	static int[] my_C;
  	public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads){
	    // TODO: Implement your parallel merge function

		my_A = A;
	    my_B = B;
	    my_C = C;
	    int A_ind = 0;
	    int B_ind = 0;

	    ExecutorService ex = Executors.newFixedThreadPool(numThreads);

	    for(int i=0;i<A.length+B.length; i+=1){

	    	if(A_ind>=A.length){
	    		ex.submit(new myClass(i, B[B_ind++]));
	    	}
	    	else if(B_ind>=B.length){
	    		ex.submit(new myClass(i, A[A_ind++]));
	    	}
	    	else if(A[A_ind] < B[B_ind]){
	    		ex.submit(new myClass(i, A[A_ind++]));
	    	}
	    	else{
	    		ex.submit(new myClass(i, B[B_ind++]));
	    	}
	    	
	    }

	    ex.shutdown();
	    try {
		    if (!ex.awaitTermination(10, TimeUnit.SECONDS)) {
		        ex.shutdownNow(); 

		        if (!ex.awaitTermination(10, TimeUnit.SECONDS))
		           System.err.println("Pool did not terminate");
		    }
	   	} catch (InterruptedException ie) {
	     	ex.shutdownNow();
	     	Thread.currentThread().interrupt();
	    }


 	}

	static class myClass implements Runnable{

		int val;
	  	int c_i;
	  	myClass(int i, int v){
	  		val = v;
	  		c_i	=i; 

	  	}
	  	public void run(){
	  		my_C[c_i] = val;
	  	}
	}


}	