import java.util.Arrays;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class TestMerge {

		public static void main (String[] args) {
				String name = args[0];
				StringBuilder result = new StringBuilder();

				int score = 0;
				for(int i = 1; i <= 50; i +=2) {
						Random rand = new Random();
						String s = String.valueOf(i+1);
						int[] a = readTest("testMerge"+i+".txt");
						int[] b = readTest("testMerge"+s+".txt");
						if(verifyParallelMerge(a, b, rand.nextInt(200))) {
								score ++;
								result.append("test "+i +": pass \n");
						} else result.append("test "+i + ": fail \n");
				}
				writeToFile(result.toString(), name + "/resultMerge.txt");
				writeToFile(name + " : "+ score + "\n", "scoresMerge.txt");
		}

		public static void merge(int[] A, int[] B, int[] C) {
				int h = 0, i = 0, j = 0;
				while(i < A.length || j < B.length) {
						if(i == A.length) {
								C[h ++] = B[j ++];
						} else if(j == B.length) {
								C[h ++] = A[i ++];
						} else {
								if(A[i] < B[j]) C[h ++] = A[i ++];
								else C[h ++] = B[j ++];
						}
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

		static boolean verifyParallelMerge(int[] A, int[] B, int num) {
				int[] C = new int[A.length + B.length];
				int[] D = new int[A.length + B.length];


				merge(A, B, C);

				try{	
				PMerge.parallelMerge(A, B, D, num);
				} catch (Exception e) {
				}

				for (int i = 0; i < C.length; i++) {
						if (C[i] != D[i]) {
								return false;
						}
				}
				return true;

		}
}
