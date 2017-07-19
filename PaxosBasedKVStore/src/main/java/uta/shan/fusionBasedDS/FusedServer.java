package uta.shan.fusionBasedDS;

import uta.shan.communication.Messager;
import uta.shan.communication.Util;
import uta.shan.config.ConfigReader;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by xz on 6/7/17.
 */
public class FusedServer<K,V> extends Server<K,V>{
    private Listener listener;
    private FusedMap<K,V> fusedMap;
    private String me;
    private ReentrantLock lock;

    public FusedServer(int port,int numPrimaries,int bid) {
        fusedMap = new FusedMap<>(numPrimaries);
        id = bid;
        listener = new Listener(port,this);
        listener.start();
        lock = new ReentrantLock();
    }

    @Override
    public void shutDown() {
        try {
            listener.getServerSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Reply<V> handleRequest(Request<K,V> request, Socket socket) {
        lock.lock();
        RequestType type = request.getType();
        K key = request.getKey();
        V first = request.getFirst();
        V second = request.getSecond();
        Reply<V> reply = new Reply<>();
        int pid = request.getId();

        if(type == RequestType.PUT) {
            fusedMap.put(key,first,second,pid,id);
            if(Util.DEBUG) {
                FusedNode<Integer> fnode = (FusedNode<Integer>) getMap().getDataStack().getHeadNode();
                System.out.println(fnode.getValue()+" "+fnode.getRefCount());
            }
            reply.setStatus(Status.OK);
        } else if(type == RequestType.REMOVE) {
            boolean ok = fusedMap.remove(key,first,second, id,pid);
            reply.setStatus(ok ? Status.OK : Status.ERR);
        } else if(type == RequestType.RECOVER) {
            Messager.sendMsg(fusedMap,socket);
            reply.setStatus(Status.OK);
        } else {
            try {
                throw new Exception("Invalid operation!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        lock.unlock();
        return reply;
    }

    public FusedMap<K,V> getMap() {
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
        FusedServer<Integer, Integer> fusedServer = new FusedServer<>(backupPorts[me],primaryHosts.length,me);
    }
}
