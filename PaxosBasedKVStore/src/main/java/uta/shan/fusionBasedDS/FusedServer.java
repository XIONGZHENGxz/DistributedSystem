package uta.shan.fusionBasedDS;

import uta.shan.communication.Messager;
import uta.shan.communication.Util;
import uta.shan.config.ConfigReader;

import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * Created by xz on 6/7/17.
 */
public class FusedServer extends Server{
    private Listener listener;
    private FusedMap<Integer> fusedMap;
    private String me;

    public FusedServer(int port,int numPrimaries,int bid) {
        fusedMap = new FusedMap<>(numPrimaries);
        id = bid;
        listener = new Listener(port,this);
        listener.start();
    }

    @Override
    public void shutDown() {
        try {
            listener.getServerSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String handleRequest(String msg, Socket socket) {
        StringTokenizer st = new StringTokenizer(msg);
        String arg = st.nextToken();
        if(arg.equals("put")) {
            int key = Integer.parseInt(st.nextToken());
            int newVal = Integer.parseInt(st.nextToken());
            int oldVal = Integer.parseInt(st.nextToken());
            int pid = Integer.parseInt(st.nextToken());
            fusedMap.put(key,newVal,oldVal,pid,id);
            if(Util.DEBUG) {
                System.out.println("debug...");
                FusedNode<Integer> fnode = (FusedNode<Integer>) getMap().getDataStack().getHeadNode();
                System.out.println(fnode.getValue()+" "+fnode.getRefCount());
            }
            return "update success!";
        } else if(arg.equals("remove")) {
            int key = Integer.parseInt(st.nextToken());
            int valToRemove = Integer.parseInt(st.nextToken());
            int valOfLast = Integer.parseInt(st.nextToken());
            int pid = Integer.parseInt(st.nextToken());
            boolean ok = fusedMap.remove(key,valToRemove,valOfLast, id,pid);
            return "remove "+(ok?"success":"failure");
        } else if(arg.equals("recover")) {
            Messager.sendMsg(fusedMap,socket);
            return null;
        } else {
            try {
                throw new Exception("Invalid operation!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public FusedMap<Integer> getMap() {
        return this.fusedMap;
    }


    public static void main(String...args) {
        int me = Integer.parseInt(args[1]);
        int[] nums = new int[2];
        ConfigReader.readNumbers(args[0],nums);
        String[] backupHosts = new String[nums[1]];
        int[] backupPorts = new int[nums[1]];
        int[] primaryPorts = new int[nums[0]];
        String[] primaryHosts = new String[nums[0]];
        ConfigReader.readJson(args[0],primaryHosts,backupHosts,primaryPorts,backupPorts);
        FusedServer fusedServer = new FusedServer(backupPorts[me],primaryHosts.length,me);
    }
}
