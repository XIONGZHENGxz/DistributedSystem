import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPThread implements Runnable {
    int port;
    BookServer bookServer;
    ServerSocket listen;
    public TCPThread(int port, BookServer bs) {
        this.port = port;
        this.bookServer = bs;
        listen = null;
        System.out.println("making:"+port);
        try {
            listen = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
            try {

                while(true) {
                    Socket connection = listen.accept();
                    BufferedReader inputStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                    String t = inputStream.readLine();
                    System.out.println("TCP COMMAND:" + t);
                    String toRet = bookServer.messageHandler(t);
                    String[] lines = toRet.split("\r\n|\r|\n");
                    int numLines = lines.length;
                    outputStream.write(numLines);
                    outputStream.writeBytes(toRet + "\n");
                    if(toRet != null) {
                        connection.close();
                        listen.close();
                        System.out.println("closing"+listen.getLocalPort());
                        return;
                    }
                }
//                    connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

    }
}

