//UT-EID=GMB974 and AT35236


import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveTask<ArrayList<Integer>>{
	ArrayList<Integer> arr;
	final int begin;
	final int mid;
	final int end;

	public PSort(int[] A, int begin, int end){
		arr = new ArrayList<Integer>(A.length);
		for(int i=0; i<A.length; i++){
			arr.add(A[i]);
		}
		this.begin = begin;
		this.mid = (begin+end)/2;
		this.end = end;
	}

	public PSort(ArrayList<Integer> arr, int begin, int end){
		this.arr = arr;
		this.begin = begin;
		this.mid = (begin+end)/2;
		this.end = end;
	}
	
	@Override
	protected ArrayList<Integer> compute() {
		if (end-begin <= 16) {
			return insertionSort();
		}
		else {
			PSort task1 = new PSort(arr, begin, mid);
			task1.fork();
			PSort task2 = new PSort(arr, mid, end);
			return merge(task2.compute(), task1.join());
		}	
	}
	
	public ArrayList<Integer> insertionSort() {
		ArrayList<Integer> sub = new ArrayList<Integer>(end-begin);
		for(int i=begin; i<end; i++){
			sub.add(arr.get(i));
		}
		for (int outer = 0; outer < sub.size(); outer++) {
			int inner = outer;
			while (inner != 0 && sub.get(inner) < sub.get(inner-1)) {
				int temp = sub.get(inner);
				sub.set(inner, sub.get(inner-1));
				sub.set(inner-1, temp);
				inner--;
			}
		}

		return sub;
	}
	
	public ArrayList<Integer> merge(ArrayList<Integer> first, ArrayList<Integer> second) {
		ArrayList<Integer> joint = new ArrayList<Integer>(first.size()+second.size());

		int f = 0;
		int s = 0;

		while (f < first.size() && s < second.size()) {
			if (first.get(f) <= second.get(s)) {
				joint.add(first.get(f));
				f++;
			}
			else{
				joint.add(second.get(s));
				s++;
			}
		}
		
		while (f < first.size()) {
			joint.add(first.get(f));
			f++;
		}
		while (s < second.size()) {
			joint.add(second.get(s));
			s++;
		}
		
		return joint;
	}

	public static void parallelSort(int[] A, int begin, int end){
		int processors = Runtime.getRuntime().availableProcessors();
	    PSort psort = new PSort(A, begin, end);
	    ForkJoinPool pool = new ForkJoinPool(processors);
	    ArrayList<Integer> result = pool.invoke(psort);
	    for(int i=0; i<A.length; i++){
	    	A[i] = result.get(i);
	    }
	}
}

		


