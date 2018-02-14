//UT-EID=


import java.util.*;
import java.util.Arrays;
import java.util.concurrent.*;


public class PMerge implements Callable<Void>{
	
	int[] A,B,C;
	int numThreads;
	
	private PMerge(int[] A, int[] B, int[]C, int numThreads){
		this.A = A;
		this.B = B;
		this.C = C;
		this.numThreads = numThreads;
	}

	public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
		ExecutorService es = Executors.newFixedThreadPool(numThreads);
		PMerge pm = new PMerge(A, B, C, numThreads);
		PMerge pm2 = new PMerge(B,A,C, numThreads);
		Future<?> f1 = es.submit(pm);
		Future<?> f2 = es.submit(pm2);
		try {
			f1.get();
			f2.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		es.shutdown();

		
  }

	public int SeqSearchRank(int[] A, int B)
	{
		int answer = 0;
		for(int i = 0; i < A.length; i++)
		{
			if(i == A.length - 1)
			{
				if(B >= A[i])
					answer = A.length;
			}
			else if(B < A[i])
			{
				answer = i;
				break;
			}
			else if (A[i] <= B && A[i +1] >= B)
				answer = i;
		}
		return answer;
	}
	   
	@Override
	public Void call() {
		
		for(int i = 0;i< A.length;i++){
			int rank = SeqSearchRank(B, A[i]);
			C[rank+i] = A[i];
		}
		return null;
	}
	
	
	
}

