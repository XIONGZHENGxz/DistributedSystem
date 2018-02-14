//UT-EID=mh43862; ksm2464


import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveAction{

  int[] a_m;
  int begin_m;
  int end_m;

  PSort(int[] a, int b, int e) {
  	this.a_m = a;
  	this.begin_m = b;
  	this.end_m = e;
  }
  public static void parallelSort(int[] A, int begin, int end){
    // TODO: Implement your parallel sort function 

    int processors = Runtime.getRuntime().availableProcessors();
    PSort p = new PSort(A, begin, end);
    ForkJoinPool pool = new ForkJoinPool(processors);
	pool.invoke(p);
  }

  @Override
  protected void compute(){
  	// if array size is less than or equal to 16 then do sequential insert sort
  	if(end_m - begin_m <= 16){
  		Arrays.sort(a_m, begin_m, end_m);
  	}
  	else {
  		int middle = ((end_m+begin_m)/2)+1;
  		invokeAll(new PSort(a_m, begin_m, middle), new PSort(a_m, middle, end_m));
  		merge(begin_m, middle, end_m);
  		
  	}
  }

  public void merge(int begin_m, int middle, int end_m){
	int[] temp = new int[middle-begin_m];
	for(int i=0;i<temp.length;i+=1){
		temp[i] = a_m[begin_m+i];
	}
	int first_pointer=0;
	int second_pointer=middle;

	for(int i=begin_m; i<end_m; i+=1){
		if(first_pointer>=temp.length){
			a_m[i] = a_m[second_pointer++];
		}
		else if(second_pointer==end_m){
			a_m[i] = temp[first_pointer++];
		}
		else if(temp[first_pointer]<a_m[second_pointer]){
			a_m[i] = temp[first_pointer++];
		}
		else{
			a_m[i] = a_m[second_pointer++];
		}
	}
  }


}