package uta.shan.communication;


/**
 * Created by xz on 6/7/17.
 */
public class Util {
    public static final int TIMEOUT = 500;
    public static final int paxosPort = 9999;
    public static final int clientPort = 8888;
    public static final boolean DEBUG = true;
    public static long getCurrTime() {
        return System.currentTimeMillis();
    }
}
