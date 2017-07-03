package uta.shan.replicationBasedDS;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import uta.shan.communication.Messager;
import uta.shan.communication.Util;

/**
 * Created by xz on 6/7/17.
 */

public class Listener<K,V> extends Thread{
    private int port;
    private ServerSocket serverSocket;
    private Server server;
    private boolean isAlive;

    public Listener(int port, Server server) {
        this.port = port;
        this.server = server;
        this.isAlive = true;
    }

    @Override
    public void run() {
        try{
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true) {
            try {
                Socket socket = serverSocket.accept();
                Request<K,V> request = (Request<K, V>) Messager.getMsg(socket);
                if(Util.DEBUG) System.out.println("get request from client: "+request.getType());
                if(request.getType().equals("shutDown")) {
                    isAlive = false;
                } else if(request.getType().equals("resume")) {
                    isAlive = true;
                }
                if(isAlive) {
                    if (request.getType().equals("getStore")) {
                        server.sendStore(socket);
                        continue;
                    } else {
                        Reply<V> reply = server.handleRequest(request);
                        Messager.sendMsg(reply, socket);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
