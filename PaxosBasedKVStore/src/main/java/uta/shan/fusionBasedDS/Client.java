package uta.shan.fusionBasedDS;

/**
 * Created by xz on 6/7/17.
 */
import org.omg.CORBA.INTERNAL;
import uta.shan.communication.Messager;

public class Client {
    private String me;//host name
    private String server;//server ip
    private int port;//server port

    public Client(String me, String server, int port) {
        this.me = me;
        this.server = server;
        this.port = port;
    }

    public String getMe() {
        return this.me;
    }

    public int get(int key) {
        String request = "get "+Integer.toString(key);
        String reply = Messager.sendAndWaitReply(request,server,port);
        return Integer.parseInt(reply);
    }

    public String put(int key, int value) {
        String request = "put "+Integer.toString(key)+" "+Integer.toString(value);
        String reply = Messager.sendAndWaitReply(request,server,port);
        return reply;
    }

    public String remove(int key) {
        String request = "remove "+Integer.toString(key);
        String reply = Messager.sendAndWaitReply(request,server,port);
        return reply;
    }
}
