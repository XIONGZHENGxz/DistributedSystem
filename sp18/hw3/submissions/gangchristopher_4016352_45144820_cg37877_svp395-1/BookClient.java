import java.util.Scanner;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;


public class BookClient {
    public static void sendUDPPacket(DatagramSocket socket, String s, InetAddress ia, int port) {
        try {
            byte[] sbuffer = new byte[1024];
            sbuffer = s.getBytes();
            DatagramPacket sPacket = new DatagramPacket(sbuffer, sbuffer.length, ia, port);
            socket.send(sPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendTCPPacket(PrintWriter out, String s) {
        try {
            out.println(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main (String[] args) throws FileNotFoundException {
        String hostAddress;
        int tcpPort;
        int udpPort;
        int clientID;
        boolean UDP = true;
        boolean initializedTCP = false;

        if (args.length != 2) {
            System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
            System.out.println("\t(1) <command-file>: file with commands to the server");
            System.out.println("\t(2) client id: an integer between 1..9");
            System.exit(-1);
        }

        String commandFile = args[0];
        clientID = Integer.parseInt(args[1]);
        hostAddress = "localhost";
        tcpPort = 7000;// hardcoded -- must match the server's tcp port
        udpPort = 8000;// hardcoded -- must match the server's udp port

        InetAddress ia;
        DatagramSocket udpSocket;
        DatagramPacket sPacket, rPacket;  // send packet, receive packet
        String[] rData;
        String send;
        byte[] sbuffer = new byte[1024];  // send buffer
        byte[] rbuffer = new byte[1024];  // receive buffer

        Socket tcpSocket;
        PrintWriter tcpOut = null;
        BufferedReader tcpIn = null;

        try {
            udpSocket = new DatagramSocket();
            ia = InetAddress.getByName(hostAddress);

            // This packet is responsible for establishing connection with the server and sending client id
            // The server should return a server port number
            send = Integer.toString(clientID) + " 123";
            sbuffer = send.getBytes();
            sPacket = new DatagramPacket(sbuffer, sbuffer.length, ia, udpPort);
            udpSocket.send(sPacket);
            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            udpSocket.receive(rPacket);

            // First element should be new port number
            rData = new String(rbuffer).split(" ");
            udpSocket.connect(ia, Integer.parseInt(rData[0]));
            udpPort = Integer.parseInt(rData[0]);

            Scanner sc = new Scanner(new FileReader(commandFile));
            while (sc.hasNextLine()) {
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");
                rbuffer = new byte[1024];
                
                if (tokens[0].equals("setmode")) {
                    if (UDP)
                        sendUDPPacket(udpSocket, cmd, ia, udpPort);
                    else 
                        sendTCPPacket(tcpOut, cmd);

                    // Switch to UDP connection
                    if (tokens[1].equals("U")) {
                        UDP = true;
                    }

                    // Connect to TCP socket server if TCP connection has not been initialized
                    else if (tokens[1].equals("T") && !initializedTCP) {
                        UDP = false;
                        initializedTCP = true;

                        // Receive unique TCP port number
                        rPacket = new DatagramPacket(rbuffer, rbuffer.length);
                        udpSocket.receive(rPacket);
                        rData = new String(rbuffer).split(" ");

                        // Initialize TCP connection with the server
                        tcpPort = Integer.parseInt(rData[0]);
                        tcpSocket = new Socket(hostAddress, tcpPort);
                        tcpOut = new PrintWriter(tcpSocket.getOutputStream(), true);
                        tcpIn = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
                    }

                    // Switch to TCP connection if TCP connection has been initialized
                    else if (tokens[1].equals("T")) {
                        UDP = false;
                    }
                } else if (tokens[0].equals("borrow")) {
                    if (UDP) {
                        sendUDPPacket(udpSocket, cmd, ia, udpPort);
                    } else {
                        sendTCPPacket(tcpOut, cmd);
                    }
                } else if (tokens[0].equals("return")) {
                    if (UDP) {
                        sendUDPPacket(udpSocket, cmd, ia, udpPort);
                    } else {
                        sendTCPPacket(tcpOut, cmd);
                    }
                } else if (tokens[0].equals("inventory")) {
                    if (UDP) {
                        sendUDPPacket(udpSocket, cmd, ia, udpPort);
                    } else {
                        sendTCPPacket(tcpOut, cmd);
                    }
                } else if (tokens[0].equals("list")) {
                    if (UDP) {
                        sendUDPPacket(udpSocket, cmd, ia, udpPort);
                    } else {
                        sendTCPPacket(tcpOut, cmd);
                    }
                } else if (tokens[0].equals("exit")) {
                    if (UDP) {
                        sendUDPPacket(udpSocket, cmd, ia, udpPort);
                    } else {
                        sendTCPPacket(tcpOut, cmd);
                    }
                } else {
                    System.out.println("ERROR: No such command");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}