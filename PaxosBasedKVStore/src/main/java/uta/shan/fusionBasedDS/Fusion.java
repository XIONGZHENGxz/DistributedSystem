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

    public static int updateCode(int oldCode, int oldVal, int newVal, int pid, int bid) {
        int newCode = oldCode + (int)Math.pow(pid,bid-1)*(newVal - oldVal);
        return newCode;
    }

    public static FusionHashMap[] recover(String[] primaryHosts, String[] fusedHosts, int[] primaryPorts, int[] fusedPorts) {
        long recoverStartTime = Util.getCurrTime();
        int n = primaryHosts.length;
        int f = fusedHosts.length;
        boolean[] flags = new boolean[n];
        FusionHashMap[] primaryMaps = getPrimaries(primaryHosts, primaryPorts, flags);
        FusedMap[] fusedMaps = getFused(fusedHosts,fusedPorts);
        //at least one good fused map
        FusedMap firstGood = null;
        for(FusedMap m: fusedMaps) {
            if(m != null) {
                firstGood = m;
                break;
            }
        }

        List<Map<Integer,FusedAuxNode>> indList = firstGood.getIndexList();
        Map<Integer,FusedAuxNode> map = indList.get(0);
        double[][] matrix = getMatrix(primaryMaps,fusedMaps);
        DoubleLinkedList dataStack = firstGood.getDataStack();
        FusedNode node = (FusedNode) dataStack.getHeadNode();
        int row = 0;
        while(node.getValue() != null) {
            List<Integer> keysOfPrimaries = new ArrayList<>();//the keys of primaries contained in this fusednode
            int firstInd = -1;//first index of primary that is good
            for(int i=0;i<n;i++) {
                FusedAuxNode auxNode = node.getAuxNode(i);
                int key = -1;
                if(auxNode !=null) {
                    key = getKey(indList.get(i),auxNode);
                    if(firstInd == -1) firstInd = i;
                }
                keysOfPrimaries.add(key);
                if(flags[i]) matrix[row++][n] = primaryMaps[i].get(key);
            }

            for(int j=0;j<f;j++) {
                if(fusedMaps[j] != null) {
                    Map<Integer,FusedAuxNode> tmp = (Map<Integer,FusedAuxNode>) fusedMaps[j].getIndexList().get(firstInd);
                    FusedNode fusedNode = tmp.get(keysOfPrimaries.get(firstInd)).getFusedNode();
                    matrix[row++][n] = (double) fusedNode.getValue();
                }
            }
            //solve linear equation
            double[] res = EquationSolver.solve(matrix);
            int j = 0;
            for(int i=0;i<n;i++) {
                if(!flags[i]) {
                    primaryMaps[i].put(keysOfPrimaries.get(i),(int)res[j++]);
                }
            }
            node = (FusedNode) node.getNext();
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
    public static double[][] getMatrix(FusionHashMap[] fusionHashMaps, FusedMap[] fusedMaps) {
        int n = fusionHashMaps.length;
        int f = fusedMaps.length;
        double[][] matrix = new double[n][n+1];
        int row = 0;
        for(int i=0;i<n;i++) {
            if(fusionHashMaps[i] !=null) {
                matrix[row++][i] = 1.0;
            }
        }
        for(int j=0;j<f;j++) {
            if(fusedMaps[j] !=null) {
                double[] r = new double[n];
                for(int i=0;i<n;i++) r[i] = Math.pow(i,j);
                matrix[row++] = r;
            }
        }
        return matrix;
    }

    public static FusionHashMap[] getPrimaries(String[] primaryHosts, int[] primaryPorts, boolean[] flags) {
        int numPrimaries = primaryHosts.length;
        FusionHashMap[] fusionHashMaps= new FusionHashMap[numPrimaries];
        for(int i=0;i<numPrimaries;i++) {
            fusionHashMaps[i] = (FusionHashMap) getData(i, primaryHosts[i], primaryPorts[i]);
            if(fusionHashMaps[i] != null) flags[i] = true;
            else{
                fusionHashMaps[i] = new FusionHashMap();
            }
        }
        return fusionHashMaps;
    }

    public static FusedMap[] getFused(String[] fusedHosts, int[] fusedPorts) {
        int numFused = fusedHosts.length;
        FusedMap[] fusedMaps = new FusedMap[numFused];
        for(int i=0;i<numFused;i++) {
            fusedMaps[i] = (FusedMap) getData(i,fusedHosts[i],fusedPorts[i]);
        }
        return fusedMaps;
    }

    public static Object getData(int i, String host, int port) {
        return Messager.sendAndWaitReply("recover",host, port);
    }

}
