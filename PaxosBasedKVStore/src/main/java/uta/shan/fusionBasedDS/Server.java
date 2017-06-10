package uta.shan.fusionBasedDS;

import java.net.Socket;

/**
 * Created by xz on 6/7/17.
 */
abstract class Server {
//    private Listener listener;
//    private String me;
//    public Server(String me) {
//        this.me = me;
//    }

    abstract String handleRequest(String requst, Socket socket);

//    public String getMe() {
//       return this.me;
//    }
}
