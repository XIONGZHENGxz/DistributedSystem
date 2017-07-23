package uta.shan.fusionBasedDS;

import uta.shan.communication.Messager;
import uta.shan.config.ConfigReader;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by xz on 6/7/17.
 */

public class PrimaryServer<K,V> extends Server<K,V> {
    private Listener listener;
    private ReentrantLock lock;
    private String[] fusedBackupHosts;
    private int[] fusedBackPorts;
    private FusionHashMap<K,V> fusionHashMap;

    public PrimaryServer(String[] hosts, int[] ports, int id, int port) {
        lock = new ReentrantLock();
        this.id = id;
        fusedBackPorts = ports;
        fusedBackupHosts = hosts;
        fusionHashMap = new FusionHashMap<>();
        listener = new Listener(port,this);
        listener.start();
    }

    public void shutDown() {
        try {
            listener.getServerSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        listener.setAlive(true);
    }

    public boolean isBound() {
        return listener.getServerSocket() != null && listener.getServerSocket().isBound();
    }

    @Override
    public Reply<V> handleRequest(Request<K,V> request, Socket socket) {
        lock.lock();
        RequestType type = request.getType();
        K key = request.getKey();
        V val = request.getFirst();
        Reply<V> reply = new Reply<V>();

        if(type == RequestType.GET) {
            if(!fusionHashMap.containsKey(key)) {
                reply.setStatus(Status.NE);
            } else {
                reply.setStatus(Status.OK);
                reply.setVal(fusionHashMap.get(key));
            }
        } else if(type == RequestType.PUT){
            V oldVal = fusionHashMap.get(key);
            fusionHashMap.put(key,val);
            sendMsgToBackup(new Request<K, V>(RequestType.PUT, key, oldVal, val, id));
            reply.setStatus(Status.OK);
        } else if(type == RequestType.REMOVE) {
            if(!fusionHashMap.containsKey(key)) reply.setStatus(Status.NE);
            else {
                V valToRemove = fusionHashMap.get(key);
                V valOfLast = fusionHashMap.getLast();
                sendMsgToBackup(new Request<K, V>(RequestType.REMOVE, key, valToRemove, valOfLast, id));
                boolean ok = fusionHashMap.remove(key);
                reply.setStatus(ok ? Status.OK : Status.ERR);
            }
        } else if(type == RequestType.RECOVER) {
            Messager.sendMsg(fusionHashMap,socket);
            reply.setStatus(Status.OK);
        } else if(type == RequestType.DOWN) {
            shutDown();
            reply = null;
        } else {
            reply.setStatus(Status.ERR);
        }
        lock.unlock();
        return reply;
    }

    public void sendMsgToBackup(Request<K,V> update) {
        for(int i=0;i<fusedBackupHosts.length;i++) {
            Messager.sendMsg(update,fusedBackupHosts[i],fusedBackPorts[i]);
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
        PrimaryServer<Integer, Integer> primaryServer = new PrimaryServer<>(backupHosts,backupPorts,me,primaryPorts[me]);
    }
}
