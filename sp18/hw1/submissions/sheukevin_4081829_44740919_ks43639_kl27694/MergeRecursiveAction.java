import java.util.*;
import java.util.concurrent.RecursiveAction;

public class MergeRecursiveAction extends RecursiveAction {
	
	int[] orig;
	int[] search;
	int[] C;
	int length;
	int index;
	Set<Integer> duplicate;
	int listNum;
	
	public MergeRecursiveAction(int[] orig, int[] search, int[] C, int length, int index, Set<Integer> duplicate, int listNum) {
		this.orig = orig;
		this.search = search;
		this.C = C;
		this.length = length;
		this.index = index;
		this.duplicate = duplicate;
		this.listNum = listNum;
	}
	
	@Override
	protected void compute() {
		if(index < length) {
			int index2 = binarySearch(search, orig[index], listNum, duplicate);
			C[index2 + index] = orig[index];
			index++;
			
			MergeRecursiveAction newAction = new MergeRecursiveAction(orig, search, C, length, index, duplicate, listNum);
			newAction.fork();
			newAction.join();
		} 
	}
	
	// Return index of x if present in search[], 
    // else return index of where it should have been
    public static int binarySearch(int search[], int x, int listNum, Set<Integer> duplicate) {
        
    	int left = 0;
        int right = search.length - 1;
        
        while (left <= right) {
            int mid = left + (right-left)/2;
 
            if (search[mid] == x) {
            	if(listNum == 2 && duplicate.contains(search[mid])) {
            		return mid + 1;
            	}
            	return mid;
            }
 
            if (search[mid] < x) {
                left = mid + 1; 
                
            } else { 
                right = mid - 1;
            }
        }
 
        // x is not found
        return left;
    }

}
