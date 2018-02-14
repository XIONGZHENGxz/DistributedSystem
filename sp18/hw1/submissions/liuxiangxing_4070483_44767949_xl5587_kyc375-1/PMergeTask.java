/*
 * EE 360P Homework 1
 * Professor Grag
 * Name: Xiangxing Liu
 * EID:  xl5587
 * Name: Kravis Cho
 * EID:  kyc375
 * Date: 02/05/2018
 */


import java.util.*;
import java.util.concurrent.*;

public class PMergeTask implements Callable<Map<Integer,Integer>> {
    private int element;
    private int[] a;
    private int index;
    private static List<Integer> dupCount = Collections.synchronizedList(new ArrayList<>());

    public PMergeTask(int element, int[] a, int index){
        this.element = element;
        this.a = a;
        this.index = index;
    }

    public static void clearSet(){
        dupCount.clear();
    }

    private int search(int[] a, int element) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] > element) {
                return i;
            }

            else if (a[i] == element) {
                synchronized (this){
                    if(dupCount.contains(element)){
                        return i + 1;
                    }

                    else {
                        dupCount.add(element);
                        return i;
                    }
                }

            }

            else if ((a[i] < element) && (i == a.length - 1)) {
                return i + 1;
            }
        }
        return -1;
    }

    @Override
    public Map<Integer,Integer> call() throws Exception {
        int index2 = search(a, element);

        if(index2 != -1) {
            index += index2;
        }else{
            return null;      //error in case the array size is less than 1
        }
        Map<Integer,Integer> temp = new HashMap<>();
        temp.put(index, element);
        return temp;
    }
}
