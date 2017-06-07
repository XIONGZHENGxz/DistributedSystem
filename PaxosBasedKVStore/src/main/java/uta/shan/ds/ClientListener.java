package uta.shan.ds;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import uta.shan.messager.Messager;

/**
 * Created by xz on 6/7/17.
 */
public class ClientListener extends Thread{
    private int port;
    private ServerSocket serverSocket;
    private Server server;
    public ClientListener(int port, Server server) {
        this.port = port;
        this.server = server;
    }

    @Override
    public void run() {
        try{
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true) {
            try {
                Socket socket = serverSocket.accept();
                Object request = Messager.getMsg(socket);
                if(request instanceof GetArg) {
                    GetReply reply = server.get((GetArg) request);
                    Messager.sendMsg(reply,socket);
                } else if(request instanceof PutArg) {
                    PutReply reply = server.put((PutArg) request);
                    Messager.sendMsg(reply,socket);
                } else {
                    System.out.println("invalid client request!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    //get message from socket
    public Object readRequest(Socket socket) {
        Object op = null;
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            op = in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return op;
    }

    public void sendReply(Object reply,Socket socket) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(reply);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
}
