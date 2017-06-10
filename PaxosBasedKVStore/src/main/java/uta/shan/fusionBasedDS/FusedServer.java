package uta.shan.fusionBasedDS;

import uta.shan.communication.Messager;
import uta.shan.communication.Util;

import java.net.Socket;
import java.util.StringTokenizer;

/**
 * Created by xz on 6/7/17.
 */
public class FusedServer extends Server{
    private Listener listener;
    private FusedMap<Integer> fusedMap;
    private String me;
    private int id;

    public FusedServer() {
//        super(me);
        listener = new Listener(Util.clientPort,this);
        listener.start();
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
            return "update success!";
        } else if(arg.equals("remove")) {
            int key = Integer.parseInt(st.nextToken());
            int valToRemove = Integer.parseInt(st.nextToken());
            int valOfLast = Integer.parseInt(st.nextToken());
            int pid = Integer.parseInt(st.nextToken());
            boolean ok = fusedMap.remove(key,valToRemove,valOfLast, pid,id);
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

}
