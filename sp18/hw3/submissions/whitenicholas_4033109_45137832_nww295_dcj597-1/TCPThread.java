//nww295
import java.io.*;
import java.net.*;

public class TCPThread implements Runnable{
    Socket sock;

    public TCPThread(Socket sock) {
        this.sock = sock;
    }
    public void run() {

        try {
            InetAddress sendAddr = sock.getInetAddress();
            int sendPort = sock.getPort();
            InputStream inStream = sock.getInputStream();
            OutputStream outStream = sock.getOutputStream();
            PrintStream print = new PrintStream(outStream);
            InputStreamReader input = new InputStreamReader(inStream);
            BufferedReader bufferRead = new BufferedReader(input);
            String read = bufferRead.readLine();
            String[] initArgs = new String[2];
            while(read != null) {
                String returnString = MultithreadedServer.processResponse(read);
                print.print(returnString);
                read = bufferRead.readLine();
                initArgs = read.split(" ");
                if(initArgs[0].equals("exit")) {
                    print.print("exit");
                    Library.printInventory();
                    print.close();
                    outStream.close();
                    bufferRead.close();
                    input.close();
                    inStream.close();
                    sock.close();
                    read = null;
                }
            }


        } catch (IOException e) {
            System.err.println(e);
        }




    }


}
