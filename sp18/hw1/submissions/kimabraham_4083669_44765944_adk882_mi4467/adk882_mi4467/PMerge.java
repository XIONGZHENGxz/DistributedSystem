//UT-EID= adk882, mi4467


import java.util.*;
import java.util.concurrent.*;
public class PMerge {
	public static int parallelMerge(int[] A, int[] B, int[] C, int numThreads) {
		//arrays A and B are sorted
		//array C is the merged array
		// your implementation goes here.
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		int sizeA = A.length;
		int sizeB = B.length;
		int partitionSize = 1;
		ArrayList <Integer> indA = new ArrayList<Integer>();
		ArrayList <Integer> indB = new ArrayList<Integer>();
		if(sizeA > sizeB){
			partitionSize = sizeA/numThreads;
			if(partitionSize < 1){
				partitionSize = 1;
			}
			int y = 0;
			for(int x = 0; x < sizeA; x+=partitionSize){
				indA.add(x);
				int oldY = y;
				if(y < sizeB){
					while(B[y]<=A[x+partitionSize]){
						y++;
						if(y == sizeB){
							break;
						}
					}
				}
				indB.add(oldY);
			}
			indA.add(sizeA);
			indB.add(sizeB);
		}
		else{
			partitionSize = sizeB/numThreads;
			if(partitionSize < 1){
				partitionSize = 1;
			}
			int y = 0;
			for(int x = 0; x < sizeB; x+=partitionSize){
				indB.add(x);
				int oldY = y;
				if(y < sizeA){
					while(A[y]<=B[x+partitionSize]){
						y++;
						if(y == sizeA){
							break;
						}
					}
				}
				indA.add(oldY);
			}
			indA.add(sizeA);
			indB.add(sizeB);
		}
		
		//Sort_Threads
		int pos = 0;
		for(int x = 0; x < indA.size()-1; x++){
			int[] A_Index = {indA.get(x), indA.get(x+1)};
			int[] B_Index = {indB.get(x), indB.get(x+1)};
			int partSize = (indA.get(x+1) - indA.get(x)) + (indB.get(x+1) - indB.get(x));
			executor.execute(new PMergeHelper(A, B, C, A_Index, B_Index, pos));
			pos += partSize;
		}
		
		
		executor.shutdown();
		while(!executor.isTerminated()){
			//NO.OP();
		}
		return 1;
	}
	private static class PMergeHelper implements Runnable {
		
		private int[] A;
		private int[] B;
		private int[] C;
		private int[] A_Index;
		private int[] B_Index;
		private int pos;
		
		public PMergeHelper(int[] A, int[] B, int[] C, int[] A_Index, int[] B_Index, int pos){
			this.A = A;
			this.B = B;
			this.C = C;
			this.A_Index = A_Index;
			this.B_Index = B_Index;
			this.pos = pos;
		}
		
		@Override
		public void run() {
			
			int offset = 0;
			int aCnt = A_Index[0];
			int bCnt = B_Index[0];
			if(B_Index[0]==B_Index[1]){
				while(aCnt<A_Index[1]){
					C[pos + offset] = A[aCnt];
					aCnt++;
					offset++;
				}
			}
			else if(A_Index[0]==A_Index[1]){
				while(bCnt<B_Index[1]){
					C[pos + offset] = B[bCnt];
					bCnt++;
					offset++;
				}
			}
			else{
				while(aCnt<A_Index[1] && bCnt<B_Index[1]){
					if(A[aCnt]<B[bCnt]){
						C[pos+offset] = A[aCnt];
						aCnt++;
					}
					else{
						C[pos+offset] = B[bCnt];
						bCnt++;
					}
					offset++;
				}
				while(aCnt<A_Index[1]){
					C[pos+offset] = A[aCnt];
					aCnt++;
					offset++;
				}
				while(bCnt<B_Index[1]){
					C[pos+offset] = B[bCnt];
					bCnt++;
					offset++;
				}
			}
		}
		
	}
}
