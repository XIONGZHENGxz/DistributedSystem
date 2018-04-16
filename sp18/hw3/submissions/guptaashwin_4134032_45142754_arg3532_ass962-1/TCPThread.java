import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPThread extends Thread{
    int tcpPort;
    private static final int maxLength = 1024;
    ServerSocket tcpSocket;

    public TCPThread(int tcpPort){
        this.tcpPort = tcpPort;
        try {
            tcpSocket = new ServerSocket(tcpPort);
        } catch (IOException e){

        }
    }

    public void run(){
        while(true){
            try {
                Socket clientSocket = tcpSocket.accept();
                //System.out.println("Accepted TCP Client");
                ServerClientThread t = new ServerClientThread(clientSocket);
                t.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
