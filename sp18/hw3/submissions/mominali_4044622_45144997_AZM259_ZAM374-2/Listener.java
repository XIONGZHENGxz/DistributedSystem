/*
 *  Listener.java
 *  EE 360P Homework 3
 *
 *  Created by Ali Ziyaan Momin and Zain Modi on 03/02/2018.
 *  EIDs: AZM259 and ZAM374
 *
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class Listener implements Runnable{

    @Override
    public void run() {
        int tcpPort = 7000;
        try {
            ServerSocket listener = new ServerSocket(tcpPort);
            Socket s;
            while ( (s = listener.accept()) != null) {
                System.out.println("Got a TCP connection");
                Thread t = new Thread(new BookServer(s));
                t.start();
            }
        } catch (IOException e) {
            System.err.println("Server aborted:" + e);
        }
    }
}
