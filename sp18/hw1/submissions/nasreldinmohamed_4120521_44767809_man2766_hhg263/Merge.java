//UT-EID= man2766, hhg263

public class Merge implements Runnable{

    private int[] A, B, C;
    private boolean flag;
    private int i;
    int val =0;

    public Merge(int[] A, int[] B, int[] C, boolean flag, int i){
        this.A = A;
        this.B = B;
        this.C = C;
        this.flag = flag;
        this.i = i;
    }


    @Override
    public void run(){
            if(!flag){
                int iValue = B[i];
                int bValue = binarySearch(iValue, A);
                C[bValue + i + val] = iValue;
                val = 0;
            }
            if(flag){
                int iValue = A[i];
                int bValue = binarySearch(iValue, B);
                C[bValue + i] = iValue;
                val =0;
            }

    }


    int binarySearch(int x, int[] arr) {
        int low = 0, high = arr.length-1;
        while(low<=high){
            int mid = (low+high)/2;
            if(arr[mid] == x) {
                val = 1;
                return mid;
            }
            else if(arr[mid] > x)
                high = mid-1;
            else
                low = mid+1;
        }
        return low;
    }
}
