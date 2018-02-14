//UT-EID=cz4723


import java.util.*;
import java.util.concurrent.*;

public class PMerge extends RecursiveAction{
	
	int[] source, dest;
	int p1, r1, p2, r2, p3;
	
	public PMerge(int[] source, int p1, int r1, int p2, int r2, int[] dest, int p3){
		this.source = source;
		this.dest = dest;
		this.p1 = p1;
		this.r1 = r1;
		this.p2 = p2;
		this.r2 = r2;
		this.p3 = p3;
	}
	
	protected void compute(){
		int l1 = r1 - p1 + 1;
		int l2 = r2 - p2 + 1;
		if(l1 < l2){
			int temp = p1;
			p1 = p2;
			p2 = temp;
			temp = r1;
			r1 = r2;
			r2 = temp;
			temp = l1;
			l1 = l2;
			l2 = temp;
		}
		if(l1 == 0) return;
		int q1 = (p1 + r1)/2;
		int q2 = binarySearch(source[q1], source, p2, r2);
		int q3 = p3 + (q1 - p1) + (q2 - p2);
		dest[q3] = source[q1];
		invokeAll(new PMerge(source, p1, q1 - 1, p2, q2 - 1, dest, p3), new PMerge(source, q1 + 1, r1, q2, r2, dest, q3 + 1));
	}
	
	private int binarySearch(int value, int[] array, int left, int right){
		int low = left;
		int high = max(left, right + 1);
		while(low < high){
			int mid = (low + high)/2;
			if(value <= array[mid]){
				high = mid;
			}
			else{
				low = mid + 1;
			}
		}
		return high;
	}
	
	private int max(int a, int b){
		return (a >= b)?a:b;
	}
	
	public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads){
		int[] source = new int[A.length + B.length];
		for(int i = 0; i < A.length; i++){
			source[i] = A[i];
		}
		for(int j = A.length; j < A.length + B.length; j++){
			source[j] = B[j-A.length];
		}
		/*
		for(int k = 0; k < source.length; k++){
			System.out.println(source[k]);
		}
		*/
		int p1 = 0;
		int r1 = A.length - 1;
		int p2 = A.length;
		int r2 = A.length + B.length - 1;
		int p3 = 0;
	
		PMerge m1 = new PMerge(source, p1, r1, p2, r2, C, p3);
		ForkJoinPool pool = new ForkJoinPool(numThreads);
		pool.invoke(m1);
	}
	
	/*
	public static void main(String[] args){
		int SIZE1 = 80;
		int SIZE2 = 100;
		int processors = Runtime.getRuntime().availableProcessors();
		int[] testArray1 = new int[SIZE1];
		int[] testArray2 = new int[SIZE2];
		for(int i = 0; i < SIZE1; i++){
			testArray1[i] = 2*i;		//consecutive even numbers
		}
		for(int i = 0; i < SIZE2; i++){
			testArray2[i] = 2*i + 1;	//consecutive odd numbers
		}
		int[] destArray = new int[SIZE1 + SIZE2];
		parallelMerge(testArray1, testArray2, destArray, processors);
		for(int i = 0; i < destArray.length; i++){
			System.out.println(destArray[i]);
		}
	}
	*/
	
}
