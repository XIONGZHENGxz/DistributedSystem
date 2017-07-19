/**
 * Created by xz on 6/9/17.
 */

package uta.shan.fusionBasedDS;

import uta.shan.communication.Messager;
import uta.shan.communication.Util;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;


public class Fusion {

    public static Object updateCode(Object oldCode, Object oldVal, Object newVal, int pid, int bid) {
        Object newCode;
        if(oldCode instanceof Integer) {
            if(oldVal == null) oldVal = 0;
            newCode = (int)oldCode + (int) Math.pow(pid + 1, bid) * ((int)newVal - (int)oldVal);
            if (Util.DEBUG) {
                System.out.println("update code...oldVal: " + oldVal + " newVal: " + newVal + " pid: " + pid + " bid: " + bid);
                System.out.println("update code...oldCode: " + oldCode + " newCode: " + newCode);
            }
            return newCode;
        } else {
            newCode = null;
        }
        return newCode;
    }

    public static FusionHashMap[] recover(String[] primaryHosts, String[] fusedHosts, int[] primaryPorts, int[] fusedPorts) {
        long recoverStartTime = Util.getCurrTime();
        int n = primaryHosts.length;
        int f = fusedHosts.length;
        boolean[] flags = new boolean[n];
        FusionHashMap<Integer, Integer>[] primaryMaps = getPrimaries(primaryHosts, primaryPorts, flags);
        boolean[] fusedFlags = new boolean[f];
        FusedMap[] fusedMaps = getFused(fusedHosts,fusedPorts,fusedFlags,n);
        if(Util.DEBUG) {
            System.out.println("got primaries and fused....");
        }
        //at least one good fused map
        FusedMap firstGood = null;
        for(int i =0;i<f;i++) {
            if(fusedFlags[i]) {
                firstGood = fusedMaps[i];
                break;
            }
        }

        List<Map<Integer,FusedAuxNode>> indList = firstGood.getIndexList();
        double[][] matrix = getMatrix(flags,fusedFlags);
        if(Util.DEBUG) EquationSolver.printMat(matrix);
        DoubleLinkedList dataStack = firstGood.getDataStack();
        FusedNode<Integer> node = (FusedNode<Integer>) dataStack.getHeadNode();
        while(true) {
            int row = 0;
            List<Integer> keysOfPrimaries = new ArrayList<>();//the keys of primaries contained in this fusednode
            int firstInd = -1;//first index of primary that is good
            for(int i=0;i<n;i++) {
                FusedAuxNode<Integer> auxNode = (FusedAuxNode<Integer>) node.getAuxNode(i);
                int key = -1;
                if(auxNode !=null) {
                    key = getKey(indList.get(i),auxNode);
                    System.out.println("k: "+key);
                    if(firstInd == -1) firstInd = i;
                }
                keysOfPrimaries.add(key);
                if(flags[i] && row<n) {
                    matrix[row++][n] = primaryMaps[i].get(key);
                }
            }

            for(int j=0;j<f;j++) {
                if(fusedFlags[j] && row<n) {
                    Map<Integer,FusedAuxNode> tmp =
                            (Map<Integer,FusedAuxNode>) fusedMaps[j].getIndexList().get(firstInd);
                    System.out.println("key: "+keysOfPrimaries.get(firstInd));
                    System.out.println(tmp.containsKey(keysOfPrimaries));
                    FusedNode fusedNode = tmp.get(keysOfPrimaries.get(firstInd)).getFusedNode();
                    Integer val = (Integer) fusedNode.getValue();
                    matrix[row++][n] = val.doubleValue();
                }
            }
            //solve linear equation

            double[] res = EquationSolver.solve(matrix);
            int j = 0;

            for(int i=0;i<n;i++) {
                if(!flags[i]) {
                    primaryMaps[i].put(keysOfPrimaries.get(i),(int)res[j]);
                }
                j++;
            }
            if(node.getNext() == dataStack.getTail()) break;
            node = (FusedNode<Integer>) node.getNext();
        }

        long recoverEndTime = Util.getCurrTime();
        System.out.println("recover time: "+(recoverEndTime-recoverStartTime));
        return primaryMaps;
    }

    //given FusedAuxNode, get its key
    public static int getKey(Map<Integer,FusedAuxNode> map, FusedAuxNode val) {
        for(int key: map.keySet()) {
            if(map.get(key) == val) return key;
        }
        return -1;
    }

    //get maxtrix
    public static double[][] getMatrix(boolean[] primaryFlags, boolean[] fusedFlags) {
        int n = primaryFlags.length;
        int f = fusedFlags.length;
        double[][] matrix = new double[n][n+1];
        for(int i=0;i<n;i++) {
            for(int j=0;j<n+1;j++) {
                matrix[i][j] = 0;
            }
        }
        int row = 0;
        for(int i=0;i<n;i++) {
            if(primaryFlags[i]) {
                matrix[row++][i] = 1.0;
                if(row == n) return matrix;
            }
        }

        for(int j=0;j<f;j++) {
            if(fusedFlags[j]) {
                for(int i=0;i<n;i++){
                    matrix[row][i] = Math.pow(i+1,j);
                }
                row++;
                if(row==n) return matrix;
            }
        }
        return matrix;
    }

    public static FusionHashMap[] getPrimaries(String[] primaryHosts, int[] primaryPorts, boolean[] flags) {
        int numPrimaries = primaryHosts.length;
        FusionHashMap[] fusionHashMaps= new FusionHashMap[numPrimaries];
        for(int i=0;i<numPrimaries;i++) {
            fusionHashMaps[i] = (FusionHashMap) getData(primaryHosts[i], primaryPorts[i]);
            if(fusionHashMaps[i] != null) flags[i] = true;
            else{
                fusionHashMaps[i] = new FusionHashMap();
            }
        }
        return fusionHashMaps;
    }

    public static FusedMap[] getFused(String[] fusedHosts, int[] fusedPorts, boolean[] flags, int numPrimaries) {
        int numFused = fusedHosts.length;
        FusedMap[] fusedMaps = new FusedMap[numFused];
        for(int i=0;i<numFused;i++) {
            if(Util.DEBUG) {
                System.out.println("try to get fused backup "+i);
            }
            fusedMaps[i] = (FusedMap) getData(fusedHosts[i],fusedPorts[i]);
            if(fusedMaps[i] != null) {
                flags[i] = true;
            } else {
                fusedMaps[i] = new FusedMap(numPrimaries);
            }
        }
        return fusedMaps;
    }

    public static Object getData(String host, int port) {
        if(Util.DEBUG) {
            System.out.println("Debug: try to get data from "+host+" "+port);
        }
        return Messager.sendAndWaitReply(new Request(RequestType.RECOVER,null),host, port);
    }

}
