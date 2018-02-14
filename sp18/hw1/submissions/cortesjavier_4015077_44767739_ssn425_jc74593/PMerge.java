//eid = ssn425, jc74593

import java.util.*;
import java.util.concurrent.*;

public class PMerge extends RecursiveAction {
	ArrayList<Integer> C;
	Deque<Integer> A;
	Deque<Integer> B;
	int[] c;
	boolean back;
	public PMerge(Deque<Integer>A , Deque<Integer>B, ArrayList<Integer> C, int[] c, boolean back)
	{
		this.A = A;
		this.B = B;
		if(this.C == null)
			this.C = new ArrayList<Integer>();
		else
			this.C = C;
		this.c = c;
		this.back = back;
		
	}
	public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads) {
		ForkJoinPool pool = new ForkJoinPool(numThreads);
		
		Deque<Integer> DeqA = new ArrayDeque<Integer>(A.length);
		Deque<Integer> DeqB = new ArrayDeque<Integer>(B.length);
		for(int k = 0; k < B.length; k++){
			DeqA.add(A[k]);
		}
		for(int k = 0; k < B.length; k++) {
			DeqB.add(B[k]);
		}
		PMerge p1 = new PMerge(DeqA, DeqB, null, C, false);
		PMerge p2 = new PMerge(DeqA, DeqB, null, C, true);
    // TODO: Implement your parallel merge function
		pool.submit(p1);
		pool.invoke(p2);
		p1.join();
	}
	public void compute()
	{
		if(C.size() == c.length/2){
			for(int k = 0; k < C.size(); k++){
				if(back)
					c[c.length-1-k] = C.get(k);
				else
					c[k] = C.get(k);
			}
			return;
		}else if(A.isEmpty()){
			while(!B.isEmpty())
				C.add(pop());
			for(int k = 0; k < C.size(); k++){
				if(back)
					c[c.length-1-k] = C.get(k);
				else
					c[k] = C.get(k);
			}
			return;
			
		}else if(B.isEmpty()){
			while(!A.isEmpty())
				C.add(pop());
			for(int k = 0; k < C.size(); k++){
				if(back)
					c[c.length-1-k] = C.get(k);
				else
					c[k] = C.get(k);
			}
			return;
		}
		else {
			C.add(pop());
			
		}
		this.compute();
	}
	public Integer pop()
	{
		if(A.isEmpty())
			return back ? B.removeLast() : B.removeFirst();
		else if(B.isEmpty())
			return back ? A.removeLast() : A.removeFirst();
		
		if(back)
			return A.peekLast() > B.peekLast() ? A.removeLast() : B.removeLast();
		else
			return A.peekFirst() < B.peekFirst() ? A.removeFirst() : B.removeFirst();
	}
}