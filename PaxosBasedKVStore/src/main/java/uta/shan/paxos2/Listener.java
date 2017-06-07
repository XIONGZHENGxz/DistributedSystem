package uta.shan.paxos2;

/**
 * Created by xz on 6/2/17.
 */

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

    public void run() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("socket connected...");
                ConnectionHandler handler = new ConnectionHandler(socket,paxos);
                handler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
