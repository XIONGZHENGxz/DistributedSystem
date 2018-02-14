//UT-EID=


import java.util.*;
import java.util.concurrent.*;


public class PMerge implements Callable<Integer>{
	int numThreads;
	int[] A, B, C;
	int index_A, index_B;
	Integer index_C;
	public static ExecutorService threadPool = Executors.newCachedThreadPool();
	public PMerge(int[] A, int[] B, int[]C, int index_A, int index_B, Integer index_C, int numThreads){
		this.numThreads=numThreads;
		this.A=A;
		this.B=B;
		this.C=C;
		this.index_A=index_A;
		this.index_B=index_B;
		this.index_C=index_C;
	}
  public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
    // TODO: Implement your parallel merge function
	  try{
		  ExecutorService es = Executors.newSingleThreadExecutor();
		  int inc_A=A.length/numThreads;
		  int inc_B=B.length/numThreads;
			 if(A.length<=B.length && inc_A==0){
				 inc_A++;
				 numThreads=A.length;
			  }
			 if(B.length<=A.length && inc_B==0){
				 inc_B++;
				  numThreads=B.length;
			  }
			 int inc_C=inc_A+inc_B;
		 int i=0,j=0;
		 int k = 0;
		  while (i<A.length && j<B.length){
					  for(int t=0; t< numThreads; t++){
						  PMerge m= new PMerge(A, B, C, i, j, k, numThreads);
						  Future<Integer> m1 = es.submit(m);
						  j=m1.get();
						  k=k+inc_C;
						  i=k-j;
						 
					  }
				  } 
		 es.shutdown ();
		 PMerge.threadPool.shutdown();
	  }
	  catch (Exception e) { System.err.println (e); }
  }

@Override
public Integer call() throws Exception {
	// TODO Auto-generated method stub
	int i=this.index_A,j=this.index_B;
	int k=this.index_C;
	
	int inc_A=A.length/numThreads;
	int inc_B=B.length/numThreads;
	int inc_C=inc_A+inc_B;
 
	while(i<A.length && j<B.length && k < this.index_C+inc_C){
		if(A[i]<=B[j]){
			C[k]=A[i];
			i++;
			k++;
		}
		else{
			C[k]=B[j];
			j++;
			k++;
		}
	}
	if(i!=A.length-1 && j!=B.length-1 && k!=C.length){
		return j;
	}
	if(B[j]<=A[i]){
	while(j<B.length){
			C[k]=B[j];
			j++;
			k++;
		
		}
	while(i<A.length){
			C[k]=A[i];
			i++;
			k++;
		

		}

	}
	else{
		while(i<A.length){
			C[k]=A[i];
			i++;
			k++;
		

		}
		while(j<B.length){
			C[k]=B[j];
			j++;
			k++;
		
		}
		
	}
	
	return j;
}
}

