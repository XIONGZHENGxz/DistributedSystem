import java.net.*;
import java.nio.file.Files;
import java.util.Scanner;
import java.io.*;
import java.util.*;
public class BookClient {

    private static String hostAddress;
    private static int tcpPort;
    private static int udpPort;
    private static int clientId;

    private static final int UDP = 0, TCP = 1;
    private static int connectionType;

    // UDP stuff
    private static DatagramSocket clientDatagramSocket;
    private static DatagramPacket sendPacket, receivePacket;

    // TCP stuff
    private static Socket clientTcpSocket;
    private static PrintWriter clientTcpWriter;
    private static Scanner clientTcpScanner;

    // writers for local out_clientId.txt file
    private static FileWriter fw;
    private static BufferedWriter bw;
    private static PrintWriter out;


    // driver
    public static void main (String[] args) {

//        System.out.println("Press ENTER to continue.");
//        try { System.in.read(); }
//        catch (IOException e){}

        if (args.length != 2) {
            System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
            System.out.println("\t(1) <command-file>: file with commands to the server");
            System.out.println("\t(2) client id: an integer between 1..9");
            System.exit(-1);
        }

        // initialize client
        String commandFile = args[0];
        clientId = Integer.parseInt(args[1]);

        // setup default connection type to UDP
        hostAddress = "localhost";
        tcpPort = 7000;// hardcoded -- must match the server's tcp port
        udpPort = 8000;// hardcoded -- must match the server's udp port
        connectionType = UDP;
        makeDatagramSocket();

        // create 'out_clientId.txt' file
        createOutFile();

        // read from file and perform transactions
        try {

            Scanner sc = new Scanner(new FileReader(commandFile));
            // Scanner sc = new Scanner(System.in);

            while (sc.hasNextLine()) {
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");
                String msg;

                try { Thread.sleep(10); }
                catch (InterruptedException e) {}

                switch (tokens[0]) {

                    case "setmode":
                        setmode(tokens[1]); // "U" or "T"
                        break;
                    case "borrow":    // fall through, all do same thing
                    case "return":    // that is, send cmd to server, and write response
                    case "inventory": // to the out_clientId.txt file
                    case "list":
                        String response = transaction(cmd, true);
                        appendToOutFile(response);
                        break;
                    case "exit":
                        exit();
                        break;
                    default:
                        System.out.println("ERROR: No such command");

                } // end switch statement

            } // end while loop

            sc.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    // creates the output file if it doesn't exist, and append the data to it
    private static void createOutFile() {
        String fileName = String.format("out_%d.txt",clientId);
        try {
            Files.deleteIfExists(new File(fileName).toPath()); // delete if exists
            fw = new FileWriter(fileName, true); // now start appending on this file
            bw = new BufferedWriter(fw);
            out = new PrintWriter(bw);
        } catch (IOException e) {
            throw new RuntimeException("Exception while creating out file!");
        }
    }

    private static void appendToOutFile(String response) {
        //System.out.println(String.format("writing: %s",response));
        if (out == null)
            throw new NullPointerException("PrintWriter cannot be null!");
        else if (response != null && !response.trim().isEmpty())
            out.println(response.trim());
    }

    // creates a datagram socket
    private static void makeDatagramSocket() {
        try {
            clientDatagramSocket = new DatagramSocket();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Exception while creating client UDP socket.");
        }
    }

    // creates a TCP socket
    private static void makeTcpSocket() {
        try {
            clientTcpSocket = new Socket(hostAddress, tcpPort);
            clientTcpWriter = new PrintWriter(clientTcpSocket.getOutputStream());
            clientTcpScanner = new Scanner(clientTcpSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Exception while creating TCP socket and readers/writers!");
        }
    }

    // closes the datagram socket
    private static void closeDatagramSocket() {
        clientDatagramSocket.close();
    }

    // closes the TCP socket
    private static void closeTcpSocket() {
        try {
            clientTcpSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Exception while closing clientSocket!");
        }
    }

    // perform a transaction, taking care of the UDP or TCP connection
    private static String transaction(String msg, boolean waitForResponse) {
        if (msg == null) return null;
        else if (connectionType == UDP)
            return transactionUDP(msg, waitForResponse);
        else if (connectionType == TCP)
            return transactionTCP(msg, waitForResponse);
        else
            throw new IllegalStateException("Invalid protocol state");
    }

    // Performs a UDP transaction with the server, sending <msg>
    // and returning <response>
    private static String transactionUDP(String msg, boolean waitForResponse) {

        if (clientDatagramSocket == null)
            throw new NullPointerException("UDP object is null");

        byte[] receiveBuffer = new byte[1000];

        try {
            InetAddress ia = InetAddress.getByName(hostAddress);

           // send
            byte[] buffer = msg.getBytes();
            sendPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
            clientDatagramSocket.send(sendPacket);

            // receive
            if (waitForResponse) {
                receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                clientDatagramSocket.receive(receivePacket);
                return new String(receivePacket.getData(), 0,
                        receivePacket.getLength())
                        .replaceAll(":::end:::", "")
                        .trim();
            } else {
                return null;
            }

        } catch (IOException e) {
            //System.err.println(e);
            throw new RuntimeException("Whoops, couldn't interact with server!");
        }

    }

    // Performs a TCP transaction with the server, sending <msg>
    // and returning <response>
    private static String transactionTCP(String msg, boolean waitForResponse) {

        if (clientTcpScanner == null || clientTcpWriter == null || clientTcpSocket == null)
            throw new NullPointerException("TCP objects are null");

        clientTcpWriter.println(msg);
        clientTcpWriter.flush();

        if (waitForResponse) {
            while (!clientTcpScanner.hasNextLine()); // wait until has response
            StringBuilder response = new StringBuilder();
            while (clientTcpScanner.hasNextLine()) {
                String nextLine = clientTcpScanner.nextLine();
                if (nextLine.equals(":::end:::")) break;
                //System.out.println(String.format("Received: %s", nextLine));
                response.append(String.format("%s\n",nextLine));
            }
            return response.toString().trim();
        } else {
            return null;
        }

    }

    // Handles "setmode"
    // protocol is either "U" or "T"
    private static void setmode(String protocol) {

        if (protocol.equals("U") && connectionType != UDP) {

            //System.out.println("Closing TCP socket...");
            transaction("close", false);
            connectionType = UDP;
            closeTcpSocket();
            makeDatagramSocket();
            //System.out.println("Changed from TCP to UDP!");

        } else if (protocol.equals("T") && connectionType != TCP) {

            //System.out.println("Closing UDP socket...");
            transaction("close", false);
            connectionType = TCP;
            closeDatagramSocket();
            makeTcpSocket();
            //System.out.println("Changed from UDP to TCP!");

        }

    }

    // close the resources for the out file, and tell the server i'm disconnecting
    private static void exit() {

        transaction(String.format("exit %d", clientId), false); // remove me from server

        if (out != null) out.close();

        try {
            if(bw != null) bw.close();
        } catch (IOException e) {
            throw new RuntimeException("Exception while closing buffered writer!");
        }
        try {
            if(fw != null) fw.close();
        } catch (IOException e) {
            throw new RuntimeException("Exception while closing file writer!");
        }

        System.exit(0);
    }

}
