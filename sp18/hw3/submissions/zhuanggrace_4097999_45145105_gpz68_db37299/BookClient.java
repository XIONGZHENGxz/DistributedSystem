
import java.net.*;
import java.util.Scanner;
import java.io.*;

public class BookClient {

    static String hostAddress;
    static InetAddress iaddr;
    static int tcpPort;
    static int udpPort;
    static boolean isUDP;
    static int clientId;
    static final int len = 1024;
    
    static Socket server;
    static Scanner in;
    static PrintStream out;
    
    
    public static void main (String[] args) {

        hostAddress = "localhost";
        tcpPort = 7000;// hardcoded -- must match the server's tcp port
        udpPort = 8000;// hardcoded -- must match the server's udp port
        isUDP = true;

        if (args.length != 2) {
            System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
            System.out.println("\t(1) <command-file>: file with commands to the server");
            System.out.println("\t(2) client id: an integer between 1..9");
            System.exit(-1);
        }
        
        // String commandFile = args[0];
        
        String commandFile = args[0];
        clientId = Integer.parseInt(args[1]);

        System.out.println("Client " + clientId + " is starting.");

        try {
            Scanner sc = new Scanner(new FileReader(commandFile));

            //Establish connection with main server
            iaddr = InetAddress.getByName(hostAddress);
            DatagramSocket datasocket = new DatagramSocket();

            //Create log output file
            FileWriter outputWriter = new FileWriter("out_" + clientId + ".txt");

            //Make initial connection to main Server
            connect(datasocket);

            while(sc.hasNextLine()) {
                String cmd = sc.nextLine();

                System.out.println("Client is about to send " + cmd + "\n");

                String[] tokens = cmd.split(" ");

                if (tokens[0].equals("setmode")) {

                    if(tokens[1].equals("U")) {
                        if(isUDP) {
                            //DO NOTHING, ALREADY IN CORRECT MODE
                        }
                        
                        else {
                            isUDP = true;
                            out.print(cmd);
                            
                            int localPort = server.getLocalPort();
                            server.close();
                            datasocket = new DatagramSocket(localPort);
    
                            byte[] rBuffer = new byte[len];
                            DatagramPacket rPacket = new DatagramPacket(rBuffer, rBuffer.length);
    
                            datasocket.receive(rPacket);
                            String retstring = new String(rPacket.getData(), 0, 11);
    
                            if(retstring.equals("Acknowledge")) {
                                System.out.println("Client received acknowledge from UDP");
                            }
    
                            udpPort = rPacket.getPort();
                            iaddr = rPacket.getAddress();
                        }
                    }
                    
                    else if(tokens[1].equals("T")) {
                        System.out.println("Is UDP" + isUDP);

                        if(!isUDP) {
                            //DO NOTHING, ALREADY IN CORRECT MODE
                        }
                        else {
                            isUDP = false;
                            byte[] buffer = new byte[cmd.length()];
                            buffer = cmd.getBytes();
                            DatagramPacket sPacket = new DatagramPacket(buffer, buffer.length, iaddr, udpPort);
                            datasocket.send(sPacket);
                            datasocket.close();

                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            tcpPort = udpPort;

                            System.out.println(tcpPort);
                            server = new Socket(iaddr, tcpPort);
                            in = new Scanner(server.getInputStream());
                            out = new PrintStream(server.getOutputStream());
                        }
                    } else {
                        System.out.println("ERROR: No such mode");
                    }

                } else if (tokens[0].equals("borrow")) {
                    if (isUDP) {
                        sendReceiveUDP(cmd, datasocket, outputWriter);
                    } else {
                        sendReceiveTCP(cmd, outputWriter);
                    }
                } else if (tokens[0].equals("return")) {
                    if (isUDP) {
                        sendReceiveUDP(cmd, datasocket, outputWriter);
                    } else {
                        sendReceiveTCP(cmd, outputWriter);
                    }
                } else if (tokens[0].equals("inventory")) {
                    if (isUDP) {
                        sendReceiveUDP(cmd, datasocket, outputWriter);
                    } else {
                        sendReceiveTCP(cmd, outputWriter);
                    }
                } else if (tokens[0].equals("list")) {
                    if (isUDP) {
                        sendReceiveUDP(cmd, datasocket, outputWriter);
                    } else {
                        sendReceiveTCP(cmd, outputWriter);
                    }
                } else if (tokens[0].equals("exit")) {
                    if (isUDP) {
                        sendReceiveUDP(cmd, datasocket, outputWriter);
                        datasocket.close();
                        outputWriter.close();
                        return;
                    } else {
                        sendReceiveTCP(cmd, outputWriter);
                        in.close();
                        out.close();
                        server.close();
                    }
                } else {
                    System.out.println("ERROR: No such command");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
                e.printStackTrace();
        } catch (SocketException e) {
            // System.out.println("Whoops, client " + clientId + "could not connect.");
            e.printStackTrace();
        } catch (IOException e) {
            // System.out.println("Whoops, client " + clientId + "could not create a file writer.");
            e.printStackTrace();
        }
    }
    
    private static void sendReceiveUDP(String cmd, DatagramSocket datasocket, FileWriter outputWriter) throws IOException {
        byte[] buffer = new byte[cmd.length()];
        buffer = cmd.getBytes();
        DatagramPacket sPacket = new DatagramPacket(buffer, buffer.length, iaddr, udpPort);
        datasocket.send(sPacket);

        //receive UDP response and write to output file
        byte[] rBuffer = new byte[len];
        DatagramPacket rPacket = new DatagramPacket(rBuffer, rBuffer.length);

        datasocket.receive(rPacket);

        String retString = new String(rPacket.getData(), 0, rPacket.getLength());
        System.out.println("Received from UDP Worker: \n" + retString + "\n");
        outputWriter.write(retString + "\n");
        outputWriter.flush();
        return;
    }

    private static void sendReceiveTCP(String cmd, FileWriter outputWriter) throws IOException {
        out.print(cmd + "\n");
        out.flush();
        
        //in.close();
        in = new Scanner(server.getInputStream());
        
        //First response is number of lines
        String received = in.nextLine();
        System.out.println("Received from TCP server: \n" + received);
        int lines = Integer.parseInt(received);
        System.out.println("Number of lines in this text chunk: " + lines + "\n");

        for(int i = 0; i < lines; i++) {
            outputWriter.write(in.nextLine() + "\n");
            outputWriter.flush();
        }
    }


    /*Connects to BookServer for initial connection. Receives response
    * with port and iaddr of UDPWorker Thread, and sets up communications. */

    private static void connect(DatagramSocket datasocket) throws IOException {
        DatagramPacket sPacket = new DatagramPacket("Connect".getBytes(), "Connect".length(), iaddr, udpPort);
        datasocket.send(sPacket);
        
        byte[] rBuffer = new byte[len];
        DatagramPacket rPacket = new DatagramPacket(rBuffer, rBuffer.length);

        datasocket.receive(rPacket);
        String retstring = new String(rPacket.getData(), 0, 11);
        
        if(retstring.equals("Acknowledge")) {
            System.out.println("Client received acknowledge from UDP");
        }

        udpPort = rPacket.getPort();
        iaddr = rPacket.getAddress();
        
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}