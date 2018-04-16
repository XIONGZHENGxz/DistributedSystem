//package hwk3;

import javax.security.sasl.SaslServer;
import java.io.IOException;
import java.net.*;

public class TCPThread extends Thread {

    int port;
    final int len = 1024;

    public TCPThread(int tcpPort) {
        port = tcpPort;
    }

    public void run(){
        ServerSocket listener;
        try {
            listener = new ServerSocket(port);
            Socket s;
            // continuously waits for clients to connect
            while((s = listener.accept()) != null){
                Thread worker = new TCPClientHandler(s);
                worker.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
