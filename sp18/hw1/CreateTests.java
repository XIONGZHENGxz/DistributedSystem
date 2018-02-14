import java.util.Arrays;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class CreateTests {

		public static void main (String[] args) {
				int[] A1 = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
				writeToFile(A1, "test1.txt");
				int[] A2 = {1, Integer.MAX_VALUE};
				writeToFile(A2, "test2.txt");
				int[] A3 = {13, 59, 24, 18, 33, 20, 11, 11, 13, 50, 10999, 97};
				writeToFile(A3, "test3.txt");

				for(int i = 4; i <= 25; i++ ) {
						int[] arr = createRandomArr(i);
						writeToFile(arr, "test"+i+".txt");
				}

				for(int i = 1; i <= 50; i++) {
					int[] arr = createRandomArr(i);
					Arrays.sort(arr);
					writeToFile(arr, "testMerge"+i+".txt");
				}
		}


		public static void writeToFile(int[] arr, String name) {
				try{
						BufferedWriter out = new BufferedWriter(new FileWriter(name));
						for(int i = 0; i < arr.length; i++) {
								out.write(arr[i]+ ",");
						}
						out.close();
				} catch(Exception e) {
				}

		}

		public static int[] createRandomArr(int k) {
				Random rand = new Random();
				int len = rand.nextInt(200);
				int[] arr = new int[len];
				for(int i = 0; i < len; i++) {
						arr[i] = rand.nextInt();
				}
				return arr;
		}

}
