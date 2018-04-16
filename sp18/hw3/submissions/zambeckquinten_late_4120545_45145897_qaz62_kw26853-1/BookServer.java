package hw3;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;
import java.util.regex.Pattern;

class ServerConnection extends Thread {
    Socket client;

    public ServerConnection(Socket s){
        this.client = s;
    }

    @Override
    public void run(){
        try{
            BufferedReader pin = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter pout = new PrintWriter(client.getOutputStream());
            String message, processed;
            while((message = pin.readLine()) != null){
                String[] tokens = message.split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                if(tokens[0].equals("exit")){
                    pin.close();
                    pout.close();
                    this.client.close();
                    break;
                }
                processed = BookServer.process_command(message);
                pout.println(processed.replace('\n', '\f'));
                pout.flush();
            }
        } catch(IOException e){
            System.out.println(e);
        }
    }
}

class ServerThread extends Thread {
    int tcpPort;

    public ServerThread(int port){
        this.tcpPort = port;
    }

    @Override
    public void run(){
        try{
            ServerSocket listener = new ServerSocket(tcpPort);
            Socket s;
            while((s = listener.accept()) != null){
                Thread t = new ServerConnection(s);
                t.start();
            }
        } catch(IOException e){
            System.err.println("Server aborted: " + e);
        }
    }
}

public class BookServer {
    static Library library;


    public static String process_command(String command){
        //String[] tokens = p.split(command);
        String[] tokens = command.split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        if(tokens[0].equals("borrow")){
            System.out.println("Borrow: " + tokens[2]);
            return library.checkoutBook(tokens[1], tokens[2]);
        }
        else if (tokens[0].equals("return")){
            System.out.println("Return: " + tokens[1]);
            return library.returnBook((Integer.parseInt(tokens[1])));
        }
        else if (tokens[0].equals("inventory")){
            System.out.println("Inventory");
            return library.listInventory();
        }
        else if (tokens[0].equals("list")){
            System.out.println("list");
            return library.listByName(tokens[1]);
        }

        return "";
    }
    public static void main (String[] args) {
        int tcpPort;
        int udpPort;
        if (args.length != 1) {
            System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
            System.exit(-1);
        }
        String fileName = args[0];
        tcpPort = 7000;
        udpPort = 8000;

        int len = 2048;

        String command;
        byte[] processed;

        // parse the inventory file
        library = new Library(fileName);

        // handle request from clients

        //TCP
        Thread tcplistener = new ServerThread(tcpPort);
        tcplistener.start();

        //UDP
        DatagramPacket datapacket, returnpacket;
        try {
            DatagramSocket datasocket = new DatagramSocket(udpPort);
            byte[] buf = new byte[len];
            while (true) {
                datapacket = new DatagramPacket(buf, buf.length);
                datasocket.receive(datapacket);
                command = new String(datapacket.getData(), 0, datapacket.getLength());
                processed = process_command(command).getBytes();
                returnpacket = new DatagramPacket(
                        processed,
                        processed.length,
                        datapacket.getAddress(),
                        datapacket.getPort());
                datasocket.send(returnpacket);
            }
        } catch (Exception e){
            System.out.println(e);
        }

    }
}

