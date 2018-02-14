//UT-EID= jl53749, kw25779


import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveAction {

	final int[] A;
	final int begin;
	final int end;

	PSort (int[] A, int begin, int end){
		this.A = A;
		this.begin = begin;
		this.end = end;
	}
	public static void parallelSort(int[] A, int begin, int end){


		//int processors = Runtime.getRuntime().availableProcessors(); //8 
		//System.out.println("Number of processors: " + processors);
		PSort p = new PSort(A, begin, end);
		ForkJoinPool pool = new ForkJoinPool(100);
		pool.invoke(p);
		//System.out.println("our array" + result);

		//personally prints out array (need to delete later)
		  /*
		  for (int i = 0 ; i < A.length ; i ++)
				System.out.print(A[i]+ " ");
			System.out.println();
		*/


		//this code is just to test if quick sort works
		
		/*
		if (end <= begin) {
			return;
		}
		int pivot = A[end-1];
		int wall = begin;
		for (int i = begin ; i < end - 1 ; i++ ) {
			if ( pivot >= A[i]) {
				int temp = A[i];
				A[i] = A[wall];
				A[wall] = temp;
				wall ++;
				
				
			}
		}
		//if (pivot < A[wall]) {
			A[end-1] = A[wall];
			A[wall] = pivot;
			wall++;
		//}
		//System.out.println("begin: " + begin + " wall:" +wall+" end: " + end);
		for (int j = 0 ; j < A.length ; j ++)
			System.out.print(A[j] + " ");
		System.out.println();
		parallelSort(A, begin , wall - 1);
		parallelSort(A, wall  , end);
		
		*/

	}

	@Override
	protected void compute() {
		//System.out.println("HI");

		if ( end <= begin){
			return; //exit
		}
		// TODO: Implement your parallel sort function


		//do insertion sort

		if (end - begin <= 16) {
			for (int i = begin ; i < end - 1 ; i++){
				for (int j = i+1 ; j > begin ; j--) {
					if (A[j] < A[j-1]) { //we swap
						int temp = A[j-1];
						A[j-1] = A[j];
						A[j] = temp;
					}
					else {
						break;
					}
				}
			}
		}

		else { //PSORT
			//quicksort
			int pivot = A[end-1];
			int wall = begin;
			for (int i = begin ; i < end - 1 ; i++ ) {
				if ( pivot >= A[i]) {
					int temp = A[i];
					A[i] = A[wall];
					A[wall] = temp;
					wall ++;


				}
			}

			A[end-1] = A[wall];
			A[wall] = pivot;
			wall++;

			//System.out.println("begin: " + begin + " wall:" +wall+" end: " + end);
			//parallelSort(A, begin , wall);
			//parallelSort(A, wall  , end);

			//forking / multithread part
			PSort Pright = new PSort (A, begin, wall-1);
			Pright.fork();
			PSort Pleft = new PSort (A, wall, end);

			Pleft.compute();
			Pright.join();

			//this secion is wrong
			/*
			int[] leftHalf =  Arrays.copyOfRange(Pleft.compute(), begin, wall);
			
			int[] rightHalf = Arrays.copyOfRange( Pright.join() , wall, end);
			
			// just a test
			for ( int i = begin ; i < wall-1 ; i ++){
				A[i] = leftHalf[i-begin];
			}
			for ( int i = wall ; i < end ; i ++){
				A[i] = rightHalf[i-wall];
			}
			for (int i = 0 ; i < A.length ; i ++)
				System.out.print(A[i]+ " ");
			System.out.println();
			*/
//			System.arraycopy(leftHalf, begin, A, begin, end-begin);
//			System.arraycopy(rightHalf, begin, A, begin, end-begin);


		}


	}




}