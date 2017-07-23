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
            if(newVal == null) newVal = 0;
            if(Util.DEBUG) {
                System.out.println("oldCode: "+oldCode);
                System.out.println("oldVal: "+oldVal);
                System.out.println("newVal: "+newVal);
            }
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

    public static int updateCode(int oldCode, int oldVal, int newVal, int pid, int bid) {
        if(Util.DEBUG) {
            System.out.println("oldCode: "+oldCode);
            System.out.println("oldVal: "+oldVal);
            System.out.println("newVal: "+newVal);
        }
        int newCode = oldCode + (int) Math.pow(pid + 1, bid) * (newVal - oldVal);
        if (Util.DEBUG) {
            System.out.println("update code...oldVal: " + oldVal + " newVal: " + newVal + " pid: " + pid + " bid: " + bid);
            System.out.println("update code...oldCode: " + oldCode + " newCode: " + newCode);
        }
        return newCode;
    }
    /*
        public static byte[] updateCode(byte[] oldCode, byte[] oldVal, byte[] newVal, int pid, int bid) {
            Object newCode;
                if(oldVal == null) oldVal = 0;
                if(newVal == null) newVal = 0;
                if(Util.DEBUG) {
                    System.out.println("oldCode: "+oldCode);
                    System.out.println("oldVal: "+oldVal);
                    System.out.println("newVal: "+newVal);
                }
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
        */
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
            int firstInd = -1;//first index of primary that is good and stored val in this node.
            for(int i=0;i<n;i++) {
                FusedAuxNode<Integer> auxNode = (FusedAuxNode<Integer>) node.getAuxNode(i);
                Integer key = null;
                if(auxNode != null) {
                    key = getKey(indList.get(i),auxNode);
                    if(firstInd == -1 && key != null) firstInd = i;
                }
                keysOfPrimaries.add(key);
                if(Util.DEBUG) System.out.println(key +" "+i +" "+flags[i]);
                if(flags[i] && row < n) {

                    if(key == null) matrix[row ++][n] = 0;
                    else matrix[row++][n] = primaryMaps[i].get(key);
                }
            }

            for(int j=0;j<f;j++) {
                if(fusedFlags[j] && row<n) {
                    Map<Integer,FusedAuxNode> tmp =
                            (Map<Integer,FusedAuxNode>) fusedMaps[j].getIndexList().get(firstInd);
                    FusedNode fusedNode = tmp.get(keysOfPrimaries.get(firstInd)).getFusedNode();
                    Integer val = (Integer) fusedNode.getValue();
                    matrix[row++][n] = val.doubleValue();
                }
            }
            //solve linear equation

            double[] res = EquationSolver.solve(matrix);
            int j = 0;

            for(int i = 0;i < n; i++) {
                Integer key = keysOfPrimaries.get(i);
                if(!flags[i] && key != null) {
                    primaryMaps[i].put(key,(int)res[j]);
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
    public static Integer getKey(Map<Integer,FusedAuxNode> map, FusedAuxNode val) {
        for(Integer key: map.keySet()) {
            if(map.get(key) == val) return key;
        }
        return null;
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
                if(row == n) return matrix;
            }
        }
        return matrix;
    }

    public static FusionHashMap<Integer, Integer>[] getPrimaries(String[] primaryHosts, int[] primaryPorts, boolean[] flags) {
        int numPrimaries = primaryHosts.length;
        FusionHashMap<Integer,Integer>[] fusionHashMaps= new FusionHashMap[numPrimaries];
        for(int i=0;i<numPrimaries;i++) {
            fusionHashMaps[i] = (FusionHashMap<Integer, Integer>) getData(primaryHosts[i], primaryPorts[i]);
            if(fusionHashMaps[i] != null) flags[i] = true;
            else{
                fusionHashMaps[i] = new FusionHashMap<>();
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
