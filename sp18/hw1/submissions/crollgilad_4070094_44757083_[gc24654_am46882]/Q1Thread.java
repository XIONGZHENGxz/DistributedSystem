//UT-EID= GC24654
//Names= Gilad Croll
//Andoni Mendoza
//UTIDs:
//gc24654
//AM46882

import java.lang.*;
import java.util.Arrays;

public class Q1Thread implements Runnable {
	int startInd;
	int endInd;
	static int[] ATemp;
	static int[] BTemp;
	static int[] CTemp;

	public Q1Thread(int s, int e){
		startInd = s;
		endInd = e;
	}

	public void run() {
		boolean curIsA = true;
		for (int i=startInd; i<endInd; i++){
			if (i<ATemp.length)
				mergeFromA(i);
			else
				mergeFromB(i-ATemp.length);
		}
	}

	void mergeFromA(int ind){
		int BArrSmallerElements = Arrays.binarySearch(BTemp,ATemp[ind]);
		if (BArrSmallerElements < 0){
			BArrSmallerElements = -BArrSmallerElements-1;
		}
		else{
			BArrSmallerElements ++;
		}
		int cIndex = ind + BArrSmallerElements ;  
		CTemp[cIndex] = ATemp[ind];
	}

	void mergeFromB(int ind){
		int AArrSmallerElements = Arrays.binarySearch(ATemp,BTemp[ind]);
		if (AArrSmallerElements < 0){
			AArrSmallerElements = -AArrSmallerElements-1;
		}
		int cIndex = ind + AArrSmallerElements ;  
		CTemp[cIndex] = BTemp[ind];		
	}

}
