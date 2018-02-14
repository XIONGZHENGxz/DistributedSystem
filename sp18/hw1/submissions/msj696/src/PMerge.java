//UT-EID = msj696


import java.util.*;
import java.util.concurrent.*;


public class PMerge
{
	public static class threadMerge implements Callable<Integer>
	{
		private int[] AorB, C;
		private boolean inA;
		private int index, value;
		
		public threadMerge(int[] AorB, int[]C, boolean inA, int index, int value)
		{
			this.AorB = AorB;
			this.C = C;
			this.inA = inA;
			this.index = index;
			this.value = value;
		}

		@Override
		public Integer call() throws Exception
		{
			try
			{
				int i;
				for(i=0; i<AorB.length; i++)
				{
					if(value < AorB[i])
					{
						break;
					}
				}
				C[index+i] = value;
			}
			catch(Exception e)
			{
				System.err.println (e);
				return 1;
			}

			return null;
		}
	}
	
	public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads)
	{
		ExecutorService es = Executors.newFixedThreadPool(numThreads);
		ArrayList<Future<Integer>> list = new ArrayList<Future<Integer>>();
		for(int i=0; i<A.length; i++)
		{
			threadMerge elem = new threadMerge(B, C, true, i, A[i]);
			list.add(es.submit(elem));
		}
		for(int i=0; i<B.length; i++)
		{
			threadMerge elem = new threadMerge(A, C, false, i, B[i]);
			list.add(es.submit(elem));
		}
		for(Future<Integer> f : list)
		{
			try {
				f.get();
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		es.shutdown ();
	}
}
