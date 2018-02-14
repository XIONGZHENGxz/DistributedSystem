/*
 * Names:    Kayvon Khosrowpour, Megan Cooper
 * Class:    EE 360P
 * UT EIDs:   knk689, mlc4285
 * Homework: 1
 */

import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveAction {
	public static int[] A;
	public int begin;
	public int end;

	PSort(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}

	public static void parallelSort(int[] Arr, int begin, int end) {
		int processors = Runtime.getRuntime().availableProcessors();
		A = Arr;
		PSort p = new PSort(begin, end);
		ForkJoinPool pool = new ForkJoinPool(processors);
		pool.invoke(p);
	}

	@Override
	protected void compute() {
		/*
		 * When the range to be sorted is 
		 * less than or equal to 16 use insert 
		 * sort to sort the range completely
		 */
		if ((end - begin) >= 16) {
			
			// Loop through all elements in the array
			for (int i = begin; i < end; i++) { 
	            
	            int index = i - 1;  // used for elements to the left of i
	            int difference = 0; // lets us know how far i has moved
	            
	            // keep switching elements with the current
	            // element if they are larger than the current 
	            // element
	            while ((index >= 0) && ((i - difference) >= 0) && (A[i - difference] < A[index])) {
	                
	            		// Switch the two values
	                int temp = A[i - difference];
	                A[i - difference] = A[index];
	                A[index] = temp;
	                
	                // update the index and
	                // difference value
	                index--;
	                difference++;
	            }
	        }
		}
		/*
		 * When the range to be sorted is greater than 16, do quick sort and then create
		 * new threads
		 */
		else {
			// Don't sort if begin is greater than end
			if (begin >= end) {
				return;
			}

			// Sort the Array with Insert Sort
			int median = ((end - begin) / 2) + begin; // choose middle as median point

			for (int i = begin; i < end; i++) {
				// Finding elements to the left that are greater
				// than the median
				if ((i < median) && (A[i] > A[median])) {
					boolean smallNumFound = false;
					int index = end;
					if (index == A.length) { // To avoid Null Pointer Exception
						index--;
					}
					// Searching for elements on the right side
					// that are smaller than the median that
					// I could swap with
					while ((index > median) && (A[i] > A[median])) {
						if (A[index] < A[median]) {
							smallNumFound = true;
							int temp = A[index];
							A[index] = A[i];
							A[i] = temp;
						}
						index--;
					}
					// If we did not find a smaller number
					// on the right, swap this number with the
					// number that is closest to the median and
					// move the median over one
					if (!smallNumFound) {

						// Stay at this index while
						// it's value is still greater
						// than the median
						while (A[i] > A[median]) {
							int temp = A[i];
							if (i < (median - 1)) {
								A[i] = A[median - 1];
								A[median - 1] = temp;
							}
							// Swap median - 1 with median
							A[median - 1] = A[median];
							A[median] = temp;
							median = median - 1; // update median index
						}
					}
				}
				// Finding elements to the right that are smaller than
				// the median
				else if ((i > median) && (A[i] < A[median])) {
					boolean largeNumFound = false;
					int index = begin;
					// Searching for elements on the left side
					// that are bigger than the median that
					// I could swap with
					while ((index < median) && (A[i] < A[median])) {
						if (A[index] > A[median]) {
							largeNumFound = true;
							int temp = A[index];
							A[index] = A[i];
							A[i] = temp;
						}
						index++;
					}

					// If we did not find a larger number
					// on the left, swap this number with the
					// number that is closest to the median and
					// move the median over one
					if (!largeNumFound) {
						// Continue until this index's
						// value is greater than the median
						while (A[i] < A[median]) {
							int temp = A[i]; // Save the value to be swapped

							if (i > (median + 1)) {
								// First swap with element one ahead of median
								A[i] = A[median + 1];
								A[median + 1] = temp;
							}
							// Now swap with median
							A[median + 1] = A[median];
							A[median] = temp;
							median = median + 1; // update median index
						}
					}
				}
			}
			
			// Create two new threads that will
			// sort everything to the left and 
			// right of the median point.
			PSort p1 = new PSort(begin, median);
			p1.fork();
			PSort p2 = new PSort(median + 1, end);
			p2.fork();
			
			// Wait for these threads to finish
			p1.join();
			p2.join();
		}

		return;
	}
}