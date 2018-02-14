
//UT-EID= cfd63, csf596

import java.util.*;
import java.util.concurrent.*;

public class PMerge implements Runnable {
	
	static final int parentA = 0;
	static final int parentB = 1;
	
	protected ArrayList<Element> elements; //Elements I must sort as a worker thread
	final int[] A, B, C; //Arrays I'm referencing
	
	PMerge(int[] A, int[] B, int[] C){
		this.elements = new ArrayList<Element>();
		this.A = A;
		this.B = B;
		this.C = C;
	}
	
	public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads) {
		
		ArrayList<Element> elements = new ArrayList<Element>();
		//merge elements from A and B into pool of elements
		for (int i = 0; i < A.length; i++) {
			elements.add(new Element(i,A[i],parentA));
		}
		for (int i = 0; i < B.length; i++) {
			elements.add(new Element(i,B[i],parentB));
		}
		
		//Create threads with roughly even amounts of elements
		PMerge threads[] = new PMerge[numThreads];
		for (int t = 0; t < numThreads; t++) {
			threads[t] = new PMerge(A,B,C);
			int elemIndex = t;
			while (elemIndex < elements.size()) {
				threads[t].elements.add(elements.get(elemIndex));
				elemIndex += numThreads;
			}
		}
		
		//Run all threads
		for (int t = 0; t < threads.length; t++) {
			threads[t].run(); 
		}
	}

	@Override
	public void run() {
		//Do work to determine position in C
		for (Element e : elements) { //for all the elements in this thread
			int otherPosition; //denotes where an element would be in input array it's not in (other array position)
			if (e.parent == parentA) { //Element form array A
				otherPosition = B.length;
				for (int i = 0; i < B.length; i++) {
					if (e.value < B[i]) {
						otherPosition = i; //found where we would be in other array
						break;
					}
				}
			}else { //Element from array B
				otherPosition = A.length;
				for (int i = 0; i < A.length; i++) {
					if (e.value <= A[i]) { //this is <= instead of < to introduce asymmetry in case of duplicates
						otherPosition = i; //found where we would be in other array
						break;
					}
				}
			}
			C[otherPosition + e.index] = e.value; //Update correct C index with element value
		}
	}
	
}

class Element{
	
	protected int index, value, parent;
	
	Element(int index, int value, int parent){
		this.index = index;
		this.value = value;
		this.parent = parent; //0=A, 1=B
	}
}