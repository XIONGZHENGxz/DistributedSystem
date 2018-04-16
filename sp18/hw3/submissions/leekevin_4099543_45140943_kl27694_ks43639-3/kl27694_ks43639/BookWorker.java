import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class BookWorker implements Runnable {
    DatagramSocket connectionUDP;
    ServerSocket connectionTCP;

    Boolean tcpMode = false;

    public BookWorker(DatagramSocket connectionUDP, ServerSocket connectionTCP){
        this.connectionUDP = connectionUDP;
        this.connectionTCP = connectionTCP;

    }

    @Override
    public void run() {

        try{
            boolean done = false;
            DatagramPacket receivepacket, returnpacket;

            while(!done) {

                if (!tcpMode) {
                    byte[] buf = new byte[1024];

                    receivepacket = new DatagramPacket(buf, buf.length);
                    connectionUDP.receive(receivepacket);

                    String command = new String(receivepacket.getData());
                    String[]tokens = command.split(" ");


                    if (tokens[0].trim().equals("setmode")) {
                        if(tokens[1].trim().equals("T")){
                            tcpMode = true;
                            byte[] rbuf = Integer.toString(connectionTCP.getLocalPort()).getBytes();
                            returnpacket = new DatagramPacket(rbuf, rbuf.length, receivepacket.getAddress(), receivepacket.getPort());
                            connectionUDP.send(returnpacket);
                        }else{
                            tcpMode = false;
                            byte[] rbuf = Integer.toString(connectionUDP.getLocalPort()).getBytes();
                            returnpacket = new DatagramPacket(rbuf, rbuf.length, receivepacket.getAddress(), receivepacket.getPort());
                            connectionUDP.send(returnpacket);
                        }


                    }else if (tokens[0].trim().equals("borrow")) {

                        byte[] rbuf = BookServer.borrowBook(tokens); //TODO: do something with response string
                        returnpacket = new DatagramPacket(rbuf, rbuf.length, receivepacket.getAddress(), receivepacket.getPort());
                        connectionUDP.send(returnpacket);

                    } else if (tokens[0].trim().equals("return")) {

                        byte[] rbuf = BookServer.returnBook(tokens);
                        returnpacket = new DatagramPacket(rbuf, rbuf.length, receivepacket.getAddress(), receivepacket.getPort());
                        connectionUDP.send(returnpacket);

                    } else if (tokens[0].trim().equals("inventory")) {

                        byte[] rbuf = BookServer.inventory();
                        returnpacket = new DatagramPacket(rbuf, rbuf.length, receivepacket.getAddress(), receivepacket.getPort());
                        connectionUDP.send(returnpacket);


                    } else if (tokens[0].trim().equals("list")) {

                        byte[] rbuf = BookServer.list(tokens);
                        returnpacket = new DatagramPacket(rbuf, rbuf.length, receivepacket.getAddress(), receivepacket.getPort());
                        connectionUDP.send(returnpacket);

                    } else if (tokens[0].trim().equals("exit")) {

                        BookServer.updateInventoryFile();

                        connectionUDP.close();
                        connectionTCP.close();

                        done = true;


                    } else {
                        System.out.println("ERROR: No such command");
                    }
                } else {
                    Socket serversocket = connectionTCP.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(serversocket.getInputStream()));
                    DataOutputStream out = new DataOutputStream(serversocket.getOutputStream());
                    String command = in.readLine();
                    String[] tokens = command.split(" ");


                    if (tokens[0].trim().equals("setmode")) {
                        if(tokens[1].trim().equals("T")){
                            tcpMode = true;
                            out.write(connectionTCP.getLocalPort());

                        }else{
                            tcpMode = false;
                            out.write(connectionTCP.getLocalPort());
                        }


                    }else if (tokens[0].trim().equals("borrow")) {

                        byte[] rbuf = BookServer.borrowBook(tokens);
                        out.write(rbuf);

                    } else if (tokens[0].trim().equals("return")) {

                        byte[] rbuf = BookServer.returnBook(tokens);
                        out.write(rbuf);

                    } else if (tokens[0].trim().equals("inventory")) {

                        byte[] rbuf = BookServer.inventory();
                        out.write(rbuf);


                    } else if (tokens[0].trim().equals("list")) {

                        byte[] rbuf = BookServer.list(tokens);
                        out.write(rbuf);

                    } else if (tokens[0].trim().equals("exit")) {

                        connectionUDP.close();
                        connectionTCP.close();
                        BookServer.updateInventoryFile();
                        done = true;


                    } else {
                        System.out.println("ERROR: No such command");
                    }

                    in.close();
                    out.close();
                    serversocket.close();
                }


            }
        }catch(Exception e){
            e.printStackTrace();
        }



    }

}
