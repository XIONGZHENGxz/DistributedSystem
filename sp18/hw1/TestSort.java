import java.util.Arrays;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class TestSort {

		public static void main (String[] args) {
				String name = args[0];
				StringBuilder result = new StringBuilder();

				int score = 0;
				for(int i = 1; i <= 25; i ++) {
						int[] test = readTest("test"+i+".txt");
						if(verifyParallelSort(test)) {
								score ++;
								result.append("test "+i +": pass \n");
						} else result.append("test "+i + ": fail \n");
				}
				writeToFile(result.toString(), name + "/result.txt");
				writeToFile(name + " : "+ score + "\n", "scores.txt");
		}

		public static void createTests() {
				int[] A1 = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
				writeToFile(A1, "test1.txt");
				int[] A2 = {1, Integer.MAX_VALUE};
				writeToFile(A2, "test2.txt");
				int[] A3 = {13, 59, 24, 18, 33, 20, 11, 11, 13, 50, 10999, 97};
				writeToFile(A3, "test3.txt");

				for(int i = 4; i <= 25; i++ ) {
						createRandomArr(i);
				}
		}

		public static int[] readTest(String file) {
				String str = "";
				try{
						BufferedReader br = new BufferedReader(new FileReader(file));
						str = br.readLine();
				} catch(Exception e) {
					e.printStackTrace();
				}
				String[] strs = str.split(",");
				int[] arr = new int[strs.length];
				for(int i = 0; i < arr.length; i++) {
						arr[i] = Integer.parseInt(strs[i]);
				}
				return arr;
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

		public static void writeToFile(String arr, String name) {
				try{
						BufferedWriter out = new BufferedWriter(new FileWriter(name, true));
						out.write(arr);
						out.close();
				} catch(Exception e) {
				}
		}

		public static int[] createRandomArr(int k) {
				Random rand = new Random();
				int len = rand.nextInt(200);
				int[] arr = new int[len];
				for(int i = 0; i < len; i++) {
						arr[i] = rand.nextInt(100000);
				}
				writeToFile(arr, "test"+k+".txt");
				return arr;
		}

		static boolean verifyParallelSort(int[] A) {
				int[] B = new int[A.length];
				System.arraycopy(A, 0, B, 0, A.length);

				Arrays.sort(A);
				PSort.parallelSort(B, 0, B.length);

				boolean isSuccess = true;

				for (int i = 0; i < A.length; i++) {

						if (A[i] != B[i]) {
								return false;
						}
				}
				return true;
		}

		public static void printArray(int[] A) {
				for (int i = 0; i < A.length; i++) {
						if (i != A.length - 1) {
								System.out.print(A[i] + " ");
						} else {
								System.out.print(A[i]);
						}
				}
				System.out.println();
		}
}
