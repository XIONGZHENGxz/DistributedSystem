import java.io.*;
import java.net.*;
import java.util.*;

public class TCPServerSocket implements Runnable {
    int connections;
    int PORT = 8000;
    BookServer bookServer;
    ServerSocket welcomeSocket = null;

    public TCPServerSocket(BookServer bookServer) {
        this.bookServer = bookServer;
        try {
            welcomeSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println("FAIL1");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String clientSentence;
        try {
            while (true) {
                Socket connectionSocket = welcomeSocket.accept();
                BufferedReader inFromClient = new BufferedReader(
                        new InputStreamReader(connectionSocket.getInputStream()));

                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                clientSentence = inFromClient.readLine();
                if(clientSentence.equals("Start")) {
                    System.out.println("Making a new tcp connection");
                    System.out.println(clientSentence);
                    PORT += 1;
                    outToClient.writeBytes("" + PORT + "\n");
                }
                else if(clientSentence != null) {
                    int port = Integer.parseInt(clientSentence);
                    TCPThread tcp = new TCPThread(port, bookServer);
                    (new Thread(tcp)).start();
                    outToClient.writeBytes("" + port + "\n");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}