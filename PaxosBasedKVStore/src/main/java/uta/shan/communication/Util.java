package uta.shan.communication;


/**
 * Created by xz on 6/7/17.
 */
public class Util {
    public static final int TIMEOUT = 5000;
    public static final int PAXOS_TIMEOUT = 1000;
    public static final int paxosPort = 9999;
    public static final int clientPort = 8888;
    public static final boolean DEBUG = true;
    public static long getCurrTime() {
        return System.currentTimeMillis();
    }
}
