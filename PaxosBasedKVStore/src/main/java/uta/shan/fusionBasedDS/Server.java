package uta.shan.fusionBasedDS;

import uta.shan.communication.Messager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by xz on 6/7/17.
 */
abstract class Server {
//    protected Listener listener;
    protected int id;

    abstract String handleRequest(String requst, Socket socket);

    public int getId() {
        return this.id;
    }

    abstract void shutDown();

}
