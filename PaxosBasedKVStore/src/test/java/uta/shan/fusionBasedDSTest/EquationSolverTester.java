package uta.shan.fusionBasedDSTest;

/**
 * Created by xz on 6/9/17.
 */

import static org.junit.Assert.*;
import org.junit.Test;
import uta.shan.fusionBasedDS.EquationSolver;

public class EquationSolverTester {

    @Test
    public void test1() {
       double[][] a = new double[][]{{1,-1,1},{2,3,3}};
       double[] x = EquationSolver.solve(a);
       assertTrue((x[0] == 1.2) && (x[1] == 0.2));
    }

    @Test
    public void test2() {
        double[][] a = new double[][]{{1, 0, 0, 2}, {0, 1, 0,3},{4, 3, 2, 10}};
        double[] x = EquationSolver.solve(a);
        assertTrue((x[0] == 2) && (x[1] == 3) && (x[2] == -3.5) );
    }

}
