import java.util.concurrent.Callable;

class ParMerge implements Callable<Void>{
	 int [] A;
	 int [] B;
	 int [] C;
	 int arrayNum;
	 int begin;
	 int end;
	 
	 public ParMerge(int []a,int []b, int[]c,int Num, int beg, int ed) {
	 A =a;
	 B =b;
	 C = c;
	 arrayNum =Num;
	 begin = beg;
	 end = ed;
	 }

	@Override
	public Void call() throws Exception {
	if(arrayNum ==0) {
		// work with array1
		int current_index = begin;
		// find index in the other array
		while(current_index<=end) {
			int other_index;
		int bSearch = binarySearch(B,0,(B.length-1),A[current_index]);
		
		if(bSearch == -1) {
			other_index = findIndex(B,0,(B.length-1),A[current_index]);
		}
		else {
			// give priority to one of the arrays
			other_index =bSearch+1;
		}
		C[current_index+other_index]=A[current_index];
		current_index++;		
		}
	}
	else {
		// work with array 2
		int current_index = begin;
		// find index in the other array
		while(current_index<=end) {
			int other_index;
		int bSearch = binarySearch(A,0,(A.length-1),B[current_index]);
		
		if(bSearch == -1) {
			other_index = findIndex(A,0,(A.length-1),B[current_index]);
		}
		else {
			// give priority to one of the arrays
			other_index = bSearch;
		}
		C[current_index+other_index]=B[current_index];
		current_index++;		
		}
	
	}
	return null;
	
	}
	
	private int binarySearch(int arr[], int b_begin, int b_end,int x)
    {
	  if (b_end>=b_begin)
        {
            int mid = (b_begin + b_end) /2;
            if (arr[mid] == x)
               return mid;
            if (arr[mid] > x) {
               return binarySearch(arr, b_begin, mid-1, x);
            }
            return binarySearch(arr, mid+1, b_end, x);
        }
		return -1;	
}
	private int findIndex(int arr [],int b_begin, int b_end,int x) {
	if(x<arr[0]) return 0;
	if(x>arr[arr.length-1]) return arr.length;
	if(b_end>=b_begin) {
		int mid = (b_begin +b_end)/2;
		if((arr[mid]<x)&&(arr[mid+1]>x)) return mid+1;
		if(arr[mid]>x) {
		 return findIndex(arr,b_begin,mid-1,x);		 		 
		}
		else {
			return findIndex(arr,mid+1,b_end,x);
		}
	}
	return -1;
 }}