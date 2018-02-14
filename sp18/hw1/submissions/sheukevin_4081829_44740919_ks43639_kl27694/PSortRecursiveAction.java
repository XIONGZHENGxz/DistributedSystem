import java.util.concurrent.RecursiveAction;

public class PSortRecursiveAction extends RecursiveAction{

	int[] A;
	int begin;
	int end;
	
	public PSortRecursiveAction(int[] A, int begin, int end) {
		this.A = A;
		this.begin = begin;
		this.end = end;
	}
	
	@Override
	protected void compute() {
		
		if((end - begin) < 16) {
			
			for (int i = begin; i <= end; i++) {
				int curr = A[i];
				int j = i-1;
		 
				while (j >= 0 && A[j] > curr) {
					A[j+1] = A[j];
					j--;
				}
				A[j+1] = curr;
			}
		} 
		
		if (begin < end) {

            int p = partition(A, begin, end);
 
            PSortRecursiveAction left= new PSortRecursiveAction(A, begin, p-1);
            PSortRecursiveAction right = new PSortRecursiveAction(A, p+1, end);
            
            left.fork();
            right.compute();
            left.join();
            
        }
	}
	
	int partition(int A[], int begin, int end) {
		
		int pivot = A[end]; 
		int pos = (begin - 1); 
		
		for (int i = begin; i < end; i++) {
			
			if (A[i] <= pivot) {
				pos++;

				int temp = A[pos];
				A[pos] = A[i];
				A[i] = temp;
			}
		}

		int temp = A[pos+1];
		A[pos+1] = A[end];
		A[end] = temp;
     
		return pos+1;
	}
}
