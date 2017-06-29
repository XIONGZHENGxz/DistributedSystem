package uta.shan.paxos2;

/**
 * Created by xz on 6/2/17.
 */

import uta.shan.communication.Messager;
import uta.shan.communication.Util;
import uta.shan.replicationBasedDS.Server;

import java.io.IOException;
import java.lang.Thread;
import java.net.ServerSocket;
import java.net.Socket;

public class Listener<T> extends Thread{
    private int port;
    private ServerSocket serverSocket;
    private Paxos<T> paxos;

    public Listener(int port, Paxos<T> paxos) {
        this.port = port;
        this.paxos = paxos;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(true) {
            try {
                Socket socket = serverSocket.accept();
                if(Util.DEBUG) {
                    System.out.println("socket connected..."+ port);
                }
                Object request = Messager.getMsg(socket);
                paxos.handleRequest(request);
            } catch (IOException e) {
                System.out.println("paxos at "+port+" down");
                break;
            }
        }
    }
}
