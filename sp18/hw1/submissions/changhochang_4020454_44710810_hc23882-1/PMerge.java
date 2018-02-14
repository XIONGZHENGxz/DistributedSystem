//UT-EID=hc23882


import java.util.*;
import java.util.concurrent.*;


public class PMerge implements Callable<Void>{
	
	private static int[] C;
	private int[] uno;
	private int[] dos;
	private boolean which;
	private int index;
	
	public PMerge(int[] uno, int[] dos, boolean which, int index) {
		this.uno = uno;
		this.dos = dos;
		this.which = which;
		this.index = index;
	}
	
	public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads) { // assumes C.length = A.length + B.length
		ExecutorService es = Executors.newFixedThreadPool(numThreads);
		PMerge.C = C;
		
		// assign tasks
		ArrayList<PMerge> tasks = new ArrayList<PMerge>();
		for (int i = 0; i < A.length; ++i) {
			tasks.add(new PMerge(A,B,false,i));
		}
		for (int i = 0; i < B.length; ++i) {
			tasks.add(new PMerge(B,A,true,i));
		}
		// run threads
		try {
			es.invokeAll(tasks);
		} catch (InterruptedException e) {}
		es.shutdown();
	}

	@Override
	public Void call() {
		// find final index
		int val = uno[index];
		int add = 0;
		for (int i = 0; i < dos.length; ++i) {
			if (dos[i] < val) {
				index++;
			}
			else if (dos[i] == val) {
				add = 1;
			}
		}
		if (which) {
			index += add; // duplicate from second array comes after
		}
		C[index] = val;
		return null;
	}
}
