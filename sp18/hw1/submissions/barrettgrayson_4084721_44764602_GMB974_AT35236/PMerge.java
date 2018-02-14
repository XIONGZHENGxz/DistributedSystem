//UT-EID=GMB974 and AT35236


import java.util.*;
import java.util.concurrent.*;


public class PMerge{
	public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
		try{
			ExecutorService es = Executors.newFixedThreadPool(numThreads);
			ArrayList<FindSpot> tasks = new ArrayList<FindSpot>(numThreads);
			int len1 = numThreads/2;
			if(len1 == 0)
				len1 = 1;
			int len2 = numThreads - (numThreads/2);
			int chunkSize1 = A.length/len1;
			int chunkSize2 = B.length/len2;

			int i = 0;
			while(i<len1){
				tasks.add(new FindSpot(C, A, B, chunkSize1*i, chunkSize1*(i+1), true));
				tasks.add(new FindSpot(C, B, A, chunkSize2*i, chunkSize2*(i+1), false));
				i++;
			}
			tasks.get(tasks.size()-2).setEnd(B.length);
			if(i < len2){
				tasks.add(new FindSpot(C, A, B, chunkSize2*i, chunkSize2*(i+1), false));
			}
			tasks.get(tasks.size()-1).setEnd(A.length);

			es.invokeAll(tasks);
			es.shutdown();
		}
		catch(Exception e){
			System.out.println("Exception Caught");
		}
	}
}

class FindSpot implements Callable<Void>{
	private int[] resArr;
	private int[] oppArr;
	private int[] myArr;
	private int begin;
	private int end;
	boolean before;

	public FindSpot(int[] resArr, int[] oppArr, int[] myArr, int begin, int end, boolean before){
		this.resArr = resArr;
		this.oppArr = oppArr;
		this.myArr = myArr;
		this.begin = begin;
		this.end = end;
		this.before = before;
	}

	public void setEnd(int end){
		this.end = end;
	}

	@Override
	public Void call(){
		for(int i=begin; i<end; i++)
			resArr[binarySearch(myArr[i], i)] = myArr[i];
		
		return null;
	}

	public int binarySearch(int val, int index){
		int low = 0;
		int high = oppArr.length-1;
		int mid;

		while(low <= high){
			mid = (low+high)/2;
			if(oppArr[mid] == val) {
				if(before)
					return mid+index;
				else
					return mid+1+index;
			}
			if(val < oppArr[mid]){
				high = mid-1;
			}
			else{
				low = mid+1;
			}
		}
		return low + index;
	}
}
