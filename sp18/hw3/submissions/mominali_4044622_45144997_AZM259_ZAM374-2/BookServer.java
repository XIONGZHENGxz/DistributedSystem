/*
 *  BookServer.java
 *  EE 360P Homework 3
 *
 *  Created by Ali Ziyaan Momin and Zain Modi on 03/02/2018.
 *  EIDs: AZM259 and ZAM374
 *
 */

import java.io.*;
import java.net.*;
import java.util.Scanner;


public class BookServer implements Runnable {

    private static Database db;
    private Socket tcpClient;

    BookServer(Socket tcpClient){
        this.tcpClient = tcpClient;
    }


    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(tcpClient.getInputStream()));
            PrintWriter pout = new PrintWriter(tcpClient.getOutputStream());
            //DataOutputStream dOut = new DataOutputStream(tcpClient.getOutputStream());

            while(true) {
                String request = reader.readLine();
                System.out.println("TCP Command from client: " + request);
                String bookName = db.getBetweenQuotes(request);
                String[] tokens = request.split(" ");
                String cmd = tokens[0];

                String ret = "";

                if(cmd.equals("borrow")){
                    String studentName = tokens[1];
                    ret = db.borrowBook(studentName, bookName);
                    db.writeInventory();
                }
                else if(cmd.equals("return")){
                    int recordID = Integer.parseInt(tokens[1]);
                    ret = db.returnBook(recordID);
                    db.writeInventory();
                }
                else if(cmd.equals("list")){
                    String studentName = tokens[1];
                    ret = db.listBorrowedBooks(studentName);
                }
                else if(cmd.equals("inventory")){
                    db.writeInventory();
                    ret = db.getInventory();
                }
                else if(cmd.equals("exit")){
                    System.out.println("Closing TCP Connection");
                    tcpClient.close();
                    break;
                }

                ret = ret.replace('\n', '\f');

                pout.println(ret);
                pout.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main (String[] args) {
        int tcpPort;
        int udpPort;

        if (args.length != 1) {
            System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
            System.exit(-1);
        }

        String fileName = args[0];
        //tcpPort = 7000;
        udpPort = 8000;

        //Initialize database
        db = new Database();

        // parse the inventory file
        try{
            Scanner sc = new Scanner(new FileReader(fileName));
            while(sc.hasNextLine()){
                String line = sc.nextLine();
                String bookName = null;

                bookName = db.getBetweenQuotes(line);
                Integer quantity = Integer.valueOf(line.replaceAll("(?:([\"'`])[^\1]*?\\1)\\s+|\r?\n", ""));

                db.booksList.put(bookName, quantity);
                db.insertionOrder.add(bookName);
            }
            System.out.println(db.booksList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        //Start TCP Listener
        Listener listener = new Listener();
        Thread t = new Thread(listener);
        t.start();

        // TODO: handle request from clients

        int len = 1024;
        DatagramPacket datapacket, returnpacket;

        try {
            DatagramSocket datasocket = new DatagramSocket(udpPort);
            byte[] buf = new byte[len];
            while (true) {
                datapacket = new DatagramPacket(buf, buf.length);
                datasocket.receive(datapacket);

                String request = new String(datapacket.getData(), 0, datapacket.getLength());
                System.out.println("UDP Command from client: " + request);
                String bookName = db.getBetweenQuotes(request);
                String[] tokens = request.split(" ");
                String cmd = tokens[0];



                byte[] ret = new byte[len];

                if(cmd.equals("borrow")){
                    String studentName = tokens[1];
                    String result = db.borrowBook(studentName, bookName);
                    ret = result.getBytes();
                    db.writeInventory();
                }
                else if(cmd.equals("return")){
                    int recordID = Integer.parseInt(tokens[1]);
                    String result = db.returnBook(recordID);
                    ret = result.getBytes();
                    db.writeInventory();
                }
                else if(cmd.equals("list")){
                    String studentName = tokens[1];
                    String result = db.listBorrowedBooks(studentName);
                    ret = result.getBytes();
                }
                else if(cmd.equals("inventory")){
                    db.writeInventory();
                    String result = db.getInventory();
                    ret = result.getBytes();
                }

                returnpacket = new DatagramPacket(
                        ret,
                        ret.length,
                        datapacket.getAddress(),
                        datapacket.getPort());

                datasocket.send(returnpacket);
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

}
