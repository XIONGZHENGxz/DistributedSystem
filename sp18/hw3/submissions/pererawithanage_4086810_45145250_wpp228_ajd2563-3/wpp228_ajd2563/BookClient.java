
import java.util.Scanner;
import java.io.*;
import java.util.*;
import java.net.*;


public class BookClient {

    private static final int MAX_BUFFER_LEN = 10000;

    private static String hostAddress = null;
    private static int tcpPort = -1;
    private static int udpPort = -1;
    private static int udpClientPort = -1;
    private static int clientId = -1;
    private static TransportProtocol transProtocol = TransportProtocol.UNINITIALIZED;
    private static DatagramSocket udpSock = null;
    private static Socket tcpSock = null;
    private static PrintWriter fileWriter;


    
    private static void errorMessage() {
        System.out.println("ERROR: No such command");
    }

    private static void initConnection(TransportProtocol tp) {
        try {
            switch (tp) {
                case UDP:
                    if (transProtocol == TransportProtocol.TCP)
                        tcpSock.close();
                    else if (transProtocol == TransportProtocol.UDP)
                        return;
                    break;
                case TCP:
                    if (transProtocol == TransportProtocol.TCP)
                        return;
                    else if (transProtocol == TransportProtocol.UDP)
                        udpSock.close();
                    break;
                case UNINITIALIZED:
                    if (transProtocol == TransportProtocol.TCP)
                        return;
                    break;
            }
        } catch (IOException e) {
            System.out.println("ERROR: Failed to close socket");
        }

        try {
            InetAddress ia = InetAddress.getByName(hostAddress);

            if (tp == TransportProtocol.TCP) {
                tcpSock = new Socket(ia, tcpPort);
            } else {
                udpSock = new DatagramSocket();
                String response = sendCommandUDP("Please send me the port.", udpPort);
                Integer port = Integer.parseInt(response.trim());

                udpClientPort = port;
            }
        } catch (UnknownHostException e) {
            System.out.println("ERROR: Failed to find host: " + hostAddress);
        } catch (SocketException e) {
            System.out.println("ERROR: Failed to open socket for host: " + hostAddress);
        } catch (IOException e) {
            System.out.println("ERROR: Failed to open socket for host: " + hostAddress);
        }

        transProtocol = tp;
    }

    private static String sendCommandTCP(String cmd) {
        try {
            PrintWriter out = new PrintWriter(tcpSock.getOutputStream(), true);
            Scanner sc = new Scanner(tcpSock.getInputStream());

            out.println(cmd);
            out.flush();

            return sc.nextLine();
        } catch (IOException e) {
            System.out.println("ERROR: Failed to send TCP packet to server.");
        }
        return "";
    }

    private static String sendCommandUDP(String cmd, int port) {
        byte[] outBuffer = cmd.getBytes();
		byte[] inBuffer = new byte[MAX_BUFFER_LEN];

        try {
			InetAddress ia = InetAddress.getByName(hostAddress);
        	DatagramPacket outPacket = new DatagramPacket(outBuffer, outBuffer.length, ia, port);
           	DatagramPacket inPacket = new DatagramPacket(inBuffer, inBuffer.length); 

            udpSock.send(outPacket);

            udpSock.receive(inPacket);
        	return new String(inPacket.getData());
        } catch (IOException e) {
            System.out.println("ERROR: Failed to send datagram to server.");
		}

		return ""; 
    }

    private static String sendCommand(String cmd) {
        if (transProtocol == TransportProtocol.TCP) {
            return sendCommandTCP(cmd);
        } else if (transProtocol == TransportProtocol.UDP) {
            return sendCommandUDP(cmd, udpClientPort);
        } else {
            System.out.println("ERROR: Internal error. Connection not initialized.");
            return null;
        }
    }
    
    public static void main (String[] args) {
        if (args.length != 2) {
            System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
            System.out.println("\t(1) <command-file>: file with commands to the server");
            System.out.println("\t(2) client id: an integer between 1..9");
            System.exit(-1);
        }

        
        String commandFile = args[0];
        clientId = Integer.parseInt(args[1]);
        hostAddress = "localhost";
        tcpPort = 7000; // hardcoded -- must match the server's tcp port
        udpPort = 8000; // hardcoded -- must match the server's udp port
        initConnection(TransportProtocol.UDP);

        
        String outputFile = "out_" + clientId + ".txt";
        try {
            fileWriter = new PrintWriter(outputFile, "UTF-8");
        } catch(Exception e) {
            System.out.println("ERROR: Failed to open output file: " + outputFile);
            return;
        }

        
        try {
            Scanner sc = new Scanner(new FileReader(commandFile));

            while(sc.hasNextLine()) {
                String cmd = sc.nextLine();
                String response = null;
                String[] tokens = cmd.split(" ");
				
                if (tokens == null || tokens.length == 0) {
                    errorMessage();
                    continue;
                }

                
                if (tokens[0].equals("setmode")) {
                    if (tokens.length != 2) {
                        errorMessage();
                        return;
                    }
                    
                    if (tokens[1].equals("U")) {
                        initConnection(TransportProtocol.UDP);
                    } else if (tokens[1].equals("T")) {
                        initConnection(TransportProtocol.TCP);
                    } else {
                        errorMessage();
                        continue;
                    }
                } else if (tokens[0].equals("borrow")    ||
                           tokens[0].equals("return")    ||
                           tokens[0].equals("inventory") ||
                           tokens[0].equals("list")) {
                    response = sendCommand(cmd);
                    fileWriter.println(response);
                } else if (tokens[0].equals("exit")) {
                    response = sendCommand(cmd);
                    fileWriter.println(response);
                    fileWriter.close();

                    if (transProtocol == TransportProtocol.TCP) {
                        try {
                            tcpSock.close();
                        } catch (Exception e) {;}
                    } else if (transProtocol == TransportProtocol.UDP) {
                        try {
                            udpSock.close();
                        } catch (Exception e) {;}
                    }

                    return;
                } else {
                    errorMessage();
                    continue;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    enum TransportProtocol { UDP, TCP, UNINITIALIZED };
}
    
