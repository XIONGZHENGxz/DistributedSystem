package uta.shan.fusionBasedDS;

import uta.shan.communication.Messager;
import uta.shan.communication.Util;
import uta.shan.config.ConfigReader;

import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by xz on 6/7/17.
 */

public class PrimaryServer extends Server {
    private Listener listener;
    private ReentrantLock lock;
    private String[] fusedBackupHosts;
    private int[] fusedBackPorts;
    private FusionHashMap fusionHashMap;

    public PrimaryServer(String[] hosts, int[] ports, int id, int port) {
        lock = new ReentrantLock();
        this.id = id;
        fusedBackPorts = ports;
        fusedBackupHosts = hosts;
        fusionHashMap = new FusionHashMap();
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
        lock.lock();
        StringTokenizer st = new StringTokenizer(msg);
        String arg = st.nextToken();
        String res = "";
        if(arg.equals("get")) {
            int key = Integer.parseInt(st.nextToken());
            if(!fusionHashMap.containsKey(key)) {
                res = "-1";
            } else {
                res = String.valueOf(fusionHashMap.get(key));
            }
        } else if(arg.equals("put")){
            int key = Integer.parseInt(st.nextToken());
            int val = Integer.parseInt(st.nextToken());
            int oldVal = fusionHashMap.get(key);
            fusionHashMap.put(key,val);
            sendMsgToBackup("put "+key+" "+val+" "+oldVal+" "+id);
            res = "put success";
        } else if(arg.equals("remove")) {
            int key = Integer.parseInt(st.nextToken());
            if(!fusionHashMap.containsKey(key)) res = "key not exists!";
            else {
                int valToRemove = fusionHashMap.get(key);
                int valOfLast = fusionHashMap.getLast();
                sendMsgToBackup("remove "+key + " " + valToRemove + " " + valOfLast + " " + id);
                boolean ok = fusionHashMap.remove(key);
                res = "remove " + (ok ? "success" : "failure");
            }
        } else if(arg.equals("recover")) {
            Messager.sendMsg(fusionHashMap,socket);
            res = null;
        } else if(arg.equals("shut down")) {
            shutDown();
            res = "ok";
        } else {
            res = "invalid operation!";
        }
        lock.unlock();
        return res;
    }

    public void sendMsgToBackup(String msg) {
        for(int i=0;i<fusedBackupHosts.length;i++) {
            Messager.sendMsg(msg,fusedBackupHosts[i],fusedBackPorts[i]);
        }
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
        PrimaryServer primaryServer = new PrimaryServer(backupHosts,backupPorts,me,primaryPorts[me]);
    }
}
