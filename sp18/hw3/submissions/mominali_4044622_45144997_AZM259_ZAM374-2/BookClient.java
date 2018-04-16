/*
 *  BookClient.java
 *  EE 360P Homework 3
 *
 *  Created by Ali Ziyaan Momin and Zain Modi on 03/02/2018.
 *  EIDs: AZM259 and ZAM374
 *
 */

import java.net.*;
import java.util.Scanner;
import java.io.*;

import java.net.*;
import java.util.Scanner;
import java.io.*;

public class BookClient {
    static Socket socket;
    static boolean tcpStart = true;

    private static void initTCP(String hostAddress, int tcpPort) throws IOException {
        if(tcpStart) {
            System.out.println("Opening TCP socket");
            socket = new Socket(hostAddress, tcpPort);
            tcpStart = false;
        }
    }

    public static void main (String[] args) {

        String hostAddress;
        int tcpPort;
        int udpPort;
        int clientId;

        if (args.length != 2) {
            System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
            System.out.println("\t(1) <command-file>: file with commands to the server");
            System.out.println("\t(2) client id: an integer between 1..9");
            System.exit(-1);
        }

        String commandFile = args[0];
        clientId = Integer.parseInt(args[1]);
        String outputFile = "out_" + Integer.toString(clientId) + ".txt";
        File cmdFile = new File(commandFile);



        hostAddress = "localhost";
        tcpPort = 7000;// hardcoded -- must match the server's tcp port
        udpPort = 8000;// hardcoded -- must match the server's udp port

        int len = 1024;
        boolean d = true;



        DatagramPacket sendPacket, receivePacket;
        byte[] buf = new byte[len];


        try {
            FileWriter fileWriter = new FileWriter(outputFile);
            PrintWriter printWriter = new PrintWriter(fileWriter);

            Scanner sc = new Scanner(new FileReader(cmdFile));


            // ----------- UDP Initialization ----------- //
            InetAddress ia = InetAddress.getByName(hostAddress);
            DatagramSocket datasocket = new DatagramSocket();


            while (true) {
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");

                if(d) {
                    //System.out.println("---> UDP CONNECTION");
                    if (tokens[0].equals("setmode")) {
                        if(tokens[1].equals("T")){
                            d = false;
                        }
                    } else if (tokens[0].equals("borrow") || tokens[0].equals("return") ||
                            tokens[0].equals("inventory") || tokens[0].equals("list")) {
                        byte[] buffer = cmd.getBytes();
                        sendPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
                        datasocket.send(sendPacket);

                        receivePacket = new DatagramPacket(buf, buf.length);
                        datasocket.receive(receivePacket);
                        String retstring = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        System.out.println(retstring);
                        printWriter.println(retstring);

                    } else if (tokens[0].equals("exit")) {
                        sc.close();
                        printWriter.close();
                        System.exit(0);
                    } else {
                        System.out.println("ERROR: No such command");
                        printWriter.println("ERROR: No such command");
                    }
                }
                else {
                    //System.out.println("---> TCP CONNECTION");
                    // ----------- TCP Initialization ----------- //
                    initTCP(hostAddress, tcpPort);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter writer = new PrintWriter(socket.getOutputStream());

                    if(tokens[0].equals("setmode")){
                        if(tokens[1].equals("U")){
                            d = true;
                            writer.println("exit");
                            writer.flush();
                            tcpStart = true;
                        }
                    } else if (tokens[0].equals("borrow") || tokens[0].equals("return") ||
                            tokens[0].equals("inventory") || tokens[0].equals("list")) {
                        writer.println(cmd);
                        writer.flush();

                        String retstring = reader.readLine().replace('\f', '\n');

                        System.out.println(retstring);
                        printWriter.println(retstring);

                    } else if (tokens[0].equals("exit")) {
                        writer.println("exit");
                        writer.flush();
                        sc.close();
                        printWriter.close();
                        System.exit(0);
                    } else {
                        System.out.println("ERROR: No such command");
                        printWriter.println("ERROR: No such command");
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
