

import java.util.Arrays;


class Task implements Runnable{
	int[] A;
	int[] B;
	int[] C;
	int start;
	int end;

	public Task(int[] A, int[] B, int[] C, int start, int end) {
		this.A = A;
		this.B = B;
		this.start = start;
		this.C = C;
		this.end = end;
	}
	@Override
	public void run() {
		for (int i = start; i < end; i++) {
		int loc;
		int ret = Arrays.binarySearch(this.A, this.B[i]);
		if (ret < 0){
			loc = 1-ret + i - 2;
		}
		else{
			loc = ret + i ;
		}
		synchAdd2(loc, this.B[i], this.C);
		}
	}	
	private static synchronized void synchAdd2(int idx, int val, int[] C) {
		if(C[idx] == val)
			C[idx+1] = val;
		else
			C[idx] = val;
	}	
}
