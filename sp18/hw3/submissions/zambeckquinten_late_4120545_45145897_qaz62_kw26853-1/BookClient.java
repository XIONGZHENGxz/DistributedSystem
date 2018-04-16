package hw3;

import java.net.*;
import java.util.Scanner;
import java.io.*;
import java.util.*;



public class BookClient {
    final static String[] COMMANDS = {"borrow", "return", "inventory", "list", "exit"};

    private enum Protocol {
        UDP
        , TCP
    }

    public static void main (String[] args) {
        String hostAddress;
        int tcpPort;
        int udpPort;
        int clientId;

        // for use with TCP
        Socket socket = null;
        BufferedReader pin = null;
        PrintWriter pout = null;

        Protocol protocol = Protocol.UDP;

        if (args.length != 2) {
            System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
            System.out.println("\t(1) <command-file>: file with commands to the server");
            System.out.println("\t(2) client id: an integer between 1..9");
            System.exit(-1);
        }

        String commandFile = args[0];
        clientId = Integer.parseInt(args[1]);

        String filename = "out_" + clientId + ".txt";


        hostAddress = "localhost";
        tcpPort = 7000;// hardcoded -- must match the server's tcp port
        udpPort = 8000;// hardcoded -- must match the server's udp port


        DatagramPacket sPacket, rPacket;
        byte[] rbuffer = new byte[1024];



        try {
            PrintWriter file = new PrintWriter(new FileWriter(filename));


            InetAddress ia = InetAddress.getByName(hostAddress);
            Scanner sc = new Scanner(new FileReader(commandFile));
            DatagramSocket datasocket = new DatagramSocket();


            while(sc.hasNextLine()) {
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");


                switch (protocol) {
                    case UDP:
                        byte[] buffer = new byte[cmd.length()];
                        buffer = cmd.getBytes();
                        if (tokens[0].equals("setmode")) {
                            if(tokens[1].equals("T")) {
                                socket = new Socket(hostAddress, tcpPort);
                                pin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                pout = new PrintWriter(socket.getOutputStream());
                                protocol = Protocol.TCP;
                            }
                        } else if (Arrays.asList(COMMANDS).contains(tokens[0])) {
                            // TODO: send appropriate command to the server and display the
                            sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
                            datasocket.send(sPacket);
                            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
                            datasocket.receive(rPacket);
                            String retstring = new String(rPacket.getData(), 0, rPacket.getLength());
                            file.println(retstring);
                            file.flush();

                        } else if (tokens[0].equals("exit")) {
                            file.close();
                        } else {
                            System.out.println("ERROR: No such command");
                        }
                        break;

                    case TCP:
                        String response;
                        if(tokens[0].equals("setmode")) {
                            if(tokens[1].equals("U")) {
                                pin.close();
                                pout.close();
                                socket.close();
                                protocol = Protocol.UDP;
                            }
                        } else if (tokens[0].equals("exit")){
                           pin.close();
                           pout.close();
                           socket.close();
                           file.close();
                           System.exit(0);
                        } else if(Arrays.asList(COMMANDS).contains(tokens[0])) {
                            pout.println(cmd);
                            pout.flush();
                            while((response = pin.readLine()) == null){
                            }
                            response = response.replace('\f', '\n');
                            file.println(response);
                            file.flush();
                        }
                        else{
                            System.out.println("ERROR: No such command");
                        }
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
