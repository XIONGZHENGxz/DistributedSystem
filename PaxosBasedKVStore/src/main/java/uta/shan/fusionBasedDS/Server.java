package uta.shan.fusionBasedDS;

import java.net.Socket;

/**
 * Created by xz on 6/7/17.
 */
abstract class Server<K,V> {
//    protected Listener listener;
    protected int id;

    abstract Reply<V> handleRequest(Request<K,V> requst, Socket socket);

    public int getId() {
        return this.id;
    }

    abstract void shutDown();

}
