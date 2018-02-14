//UT-EID= jro769 mac7865


import java.util.*;
import java.util.concurrent.*;


public class PMerge extends Thread{
	int [] A;
	int [] B;
	int [] C;
	int beg;
	int end; 
	PMerge(int[] A, int [] B, int[] C, int beg, int end){
		this.A = A;
		this.B = B;
		this.C = C;
		this.beg = beg;
		this.end = end;
	}
	public void run(){
		for(int i = beg; i<end; i++){
			if(i<A.length){
				int spot = i; 
				for (int j = 0; j<B.length; j++){
					if (A[i]> B[j]){
						spot ++; 
					}
					else{
						break; 
					}
				}
				C[spot] = A[i];
				
			}
			else{
				int temp = i; 
				i = i - A.length; 
				int spot = i; 
				for (int j = 0; j<A.length; j++){
					if (B[i]> A[j]){
						spot ++; 
					}
					else{
						break; 
					}
				}
				C[spot] = B[i];
				i = temp; 
			}
		}
	}
	public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
		int length = A.length + B.length;
		if (length < numThreads){
			numThreads = length; 
		}
		int segment_size = length/numThreads; 
		int beg = 0; 
		ArrayList<PMerge> merge_list = new ArrayList<PMerge>(); 
		int num_threads = 1; 
		for(int i = 0; i < numThreads-1; i++){
			num_threads ++;
			PMerge merge = new PMerge(A,B,C,beg,beg+segment_size);
			beg = beg + segment_size; 
			merge.start();
			merge_list.add(merge); 
		}
		System.out.println("Created " +num_threads+" new Threads");
		PMerge merge = new PMerge(A,B,C,beg,length); 
		merge.start();
		merge_list.add(merge);
		
		for(PMerge m : merge_list){
			try {
				m.join();
			} catch (InterruptedException e) {
				System.out.println("Error you messed up idiot");
			} 
		}

	}
}
