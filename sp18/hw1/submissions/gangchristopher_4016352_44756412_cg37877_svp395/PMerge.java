//UT-EID=cg37877, svp395


import java.util.*;
import java.util.concurrent.*;


public class PMerge{
	public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
		// TODO: Implement your parallel merge function
		List<PMerge.merge> threads = new ArrayList<PMerge.merge>();
		PMerge pmerge = new PMerge();
		for (int i = 0; i < numThreads; i++) {
			threads.add(pmerge.new merge(A, B, C));
		}
		
		for (int i = 0; i < A.length; i++) {
			threads.get(i % numThreads).addElement(true, i);
		}
		
		for (int i = 0; i < B.length; i++) {
			threads.get((i + A.length) % numThreads).addElement(false, i);
		}
		
		for (int i = 0; i < threads.size(); i++) {
			threads.get(i).start();
		}
		
		try {
			for (int i = 0; i < threads.size(); i++) {
				threads.get(i).join();
			} 
		} catch (Exception e) {
			System.out.println("Oops!");
		}
		
	}
	
	public class merge extends Thread {
		int[] A, B, C;
		int numElements, begin, end;
		ArrayList<Boolean> AorB = new ArrayList<Boolean>();
		ArrayList<Integer> indices = new ArrayList<Integer>();
		
		public merge(int[] A, int[] B, int[] C) {
			this.A = A;
			this.B = B;
			this.C = C;
		}
		
		// if AorB == true, then access array A, else B
		public void addElement(boolean AorB, int index) {
			this.AorB.add(AorB);
			this.indices.add(index);
		}
		
		public void run() {
			int val = 0;
			
			// numberLessThan is number of values less than current val
			int numberLessThan = 0;
			for (int i = 0; i < AorB.size(); i++) {
				numberLessThan = 0;
				if (AorB.get(i)) {
					val = A[indices.get(i)];
					numberLessThan = indices.get(i) + numLessThan(B, val);
					// System.out.println("A Val: " + val + " Index: " + numberLessThan);
				} else {
					val = B[indices.get(i)];
					numberLessThan = indices.get(i) + numLessThan(A, val);
					// System.out.println("B Val: " + val + " Index: " + numberLessThan);
				}
			}
			if (AorB.size() > 0) {
				while(C[numberLessThan] == val) numberLessThan++;
				C[numberLessThan] = val;
			}
			
		}
		
		public int numLessThan(int[] array, int val) {
			int i = 0;
			
			while (i < array.length && array[i] < val) {
				i++;
			}
			return i;
		}
		
	}
}