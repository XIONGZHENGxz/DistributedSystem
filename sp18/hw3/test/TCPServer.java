import java.io.*;
import java.net.*;
import java.util.*;

public class TCPServer extends BookServer implements Runnable {
    private int tcpPort = 7000;
    @Override
    public void run(){
        try{
            @SuppressWarnings("resource")

            Socket Tsocket;
            while((Tsocket = serverSock.accept()) != null){
                int port = Tsocket.getPort();

                PrintWriter writer = new PrintWriter(Tsocket.getOutputStream());
                ClientHandler client = new ClientHandler(Tsocket, writer);
                Thread t = new Thread(client);
                t.start();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    class ClientHandler implements Runnable{
        private Socket s;
        private PrintWriter writer;
        private Scanner reader;
        ClientHandler(Socket s, PrintWriter writer) throws IOException{
            this.s = s;
            this.writer = writer;
        }

        @Override
        public void run(){
            try {
                reader = new Scanner(s.getInputStream());
            }catch (IOException e){
                e.printStackTrace();
            }
            String message;
            while (reader.hasNext()) {
                if(DEBUG) System.out.println("Server trying to receive message");
                message = reader.nextLine();
                if(DEBUG) System.out.println("Message from ClientHandler: " + message);
                String ret = processCommand(message);
                if(DEBUG) System.out.println("Message output from ClientHandler: " + ret);

                String[] set = ret.split("\n");
                ret = "";

                for(String temp : set){
                    ret = ret + temp + "&&";
                }

                writer.println(ret);
                writer.flush();
                if(DEBUG) System.out.println("Server sent message");

                if(s.isClosed()){
                    break;
                }
            }
            if(DEBUG){System.out.println("TCP disconnect from client: " + s.getPort());}
        }
    }
}
