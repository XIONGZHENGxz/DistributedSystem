package uta.shan.fusionBasedDS;

import uta.shan.communication.Util;

import java.util.List;

/**
 * Created by xz on 6/9/17.
 */
public class EquationSolver {
    public static void printMat(double[][] A) {
        int n = A.length;
        for(int i=0;i<n;i++) {
            for(int j=0;j<n+1;j++) {
                System.out.println(A[i][j]+"\t");
            }
            System.out.println("\n");
        }
    }

    public static double[] solve(double[][] a) {
        if(Util.DEBUG) printMat(a);
        int n = a.length;
        double[][] A = new double[n][n+1];

        for(int i=0;i<n;i++) {
            for(int j=0;j<n+1;j++) {
                A[i][j] = a[i][j];
            }
        }

        for (int i=0; i<n; i++) {
            // Search for maximum in this column
            double maxEl = Math.abs(A[i][i]);
            int maxRow = i;
            for (int k=i+1; k<n; k++) {
                if (Math.abs(A[k][i]) > maxEl) {
                    maxEl = Math.abs(A[k][i]);
                    maxRow = k;
                }
            }

            // Swap maximum row with current row (column by column)
            for (int k=i; k<n+1;k++) {
                double tmp = A[maxRow][k];
                A[maxRow][k] = A[i][k];
                A[i][k] = tmp;
            }

            // Make all rows below this one 0 in current column
            for (int k=i+1; k<n; k++) {
                double c = -A[k][i]/A[i][i];
                for (int j=i; j<n+1; j++) {
                    if (i==j) {
                        A[k][j] = 0;
                    } else {
                        A[k][j] += c * A[i][j];
                    }
                }
            }
        }

        // Solve equation Ax=b for an upper triangular matrix A
        double[] x = new double[n];
        for (int i=n-1; i>=0; i--) {
            x[i] = A[i][n]/A[i][i];
            for (int k=i-1;k>=0; k--) {
                A[k][n] -= A[k][i] * x[i];
            }
        }
        if(Util.DEBUG) printResult(x);
        return x;
    }

    public static void printResult(double[] x) {
        System.out.println("result:");
        for(int i=0;i<x.length;i++) {
            System.out.println(x[i]);
        }
        System.out.println("\n");

    }
}
