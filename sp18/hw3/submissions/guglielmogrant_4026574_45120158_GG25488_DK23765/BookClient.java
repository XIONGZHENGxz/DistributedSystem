import java.util.Scanner;
import java.io.*;
import java.net.*;

public class BookClient {

    private static int clientId;
    private static String hostAddress = "localhost";
    private static int tcpPort = 7000;// hardcoded -- must match the server's tcp port
    private static int udpPort = 8000;// hardcoded -- must match the server's udp port
    private static String mode = "U";
    private static String retstring = "";
    //UDP initialization
    private static InetAddress ia;
    private static DatagramSocket datasocket;
    private static int len = 1024;
    private static DatagramPacket sPacket, rPacket;
    //TCP initialization
    private static Socket clientSocket;
    private static PrintWriter outToServer;
    private static BufferedReader inFromServer;
    private static PrintWriter writer;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
            System.out.println("\t(1) <command-file>: file with commands to the server");
            System.out.println("\t(2) client id: an integer between 1..9");
            System.exit(-1);
        }
        //setup output file and its writer
        clientId = Integer.parseInt(args[1]);
        String outputFileName = "out_" + String.valueOf(clientId) + ".txt";
        File file = new File(outputFileName);
        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            System.err.println(e);
        }
        //get command file and the client id
        String commandFile = args[0];

        try {
            setupUDP();
            setupTCP();
            Scanner sc = new Scanner(new FileReader(commandFile));

            while (sc.hasNextLine()) {
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");

                if (mode.equals("U")) {
                    sendUDPMessage(cmd);
                } else if (mode.equals("T")) {
                    sendTCPMessage(cmd);
                }

                if (tokens[0].equals("setmode")) {
                    //change from UDP to TCP
                    if (mode.equals("U") && tokens[1].equals("T")) {
                        datasocket.close();
                        clientSocket = new Socket(hostAddress, tcpPort);
                        outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
                        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    }
                    //change from TCP to UDP
                    else if (mode.equals("T") && tokens[1].equals("U")) {
                        clientSocket.close();
                        outToServer.close();
                        inFromServer.close();
                        datasocket = new DatagramSocket();
                    }
                    mode = tokens[1];
                }
                else if (tokens[0].equals("borrow") || tokens[0].equals("return")) {
                    //set and write retstring to file
                    if(mode.equals("U")) {
                        byte[] rbuffer = new byte[len];
                        rPacket = new DatagramPacket(rbuffer, rbuffer.length);
                        datasocket.receive(rPacket);
                        retstring = new String(rPacket.getData(), 0, rPacket.getLength());
                    }
                    else if(mode.equals("T")){
                        retstring = inFromServer.readLine();
                    }
                    writer.println(retstring);
                }
                else if (tokens[0].equals("inventory") || tokens[0].equals("list")) {
                    //set and write retstring to file until EOF
                	if(mode.equals("U")) {
                        byte[] rbuffer = new byte[len];
                        rPacket = new DatagramPacket(rbuffer, rbuffer.length);
                        datasocket.receive(rPacket);
                        retstring = new String(rPacket.getData(), 0, rPacket.getLength());
                    }
                    else if(mode.equals("T")) {
                        retstring = inFromServer.readLine();
                    }
                	while(!retstring.equals("EOF")) {
	                    writer.println(retstring);
	                    if(mode.equals("U")) {
	                        byte[] rbuffer = new byte[len];
	                        rPacket = new DatagramPacket(rbuffer, rbuffer.length);
	                        datasocket.receive(rPacket);
	                        retstring = new String(rPacket.getData(), 0, rPacket.getLength());
	                    }
	                    else if(mode.equals("T")) {
	                        retstring = inFromServer.readLine();
	                    }
                    }
                }
                else if (tokens[0].equals("exit")) {
                	writer.flush();
                    writer.close();
                    if(mode.equals("U")) {
                        datasocket.close();
                    }
                    else if(mode.equals("T")) {
                        clientSocket.close();
                    }
                }
                else {
                    System.out.println("ERROR: No such command");
                }
            }
            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            System.err.println(e);
        } catch (SocketException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private static void setupUDP() throws UnknownHostException, SocketException {
        ia = InetAddress.getByName(hostAddress);
        datasocket = new DatagramSocket();
    }

    private static void setupTCP() throws IOException {
        clientSocket = new Socket(hostAddress, tcpPort);
        outToServer = new PrintWriter(clientSocket.getOutputStream());
        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        clientSocket.close();
        outToServer.close();
        inFromServer.close();
    }

    private static void sendUDPMessage(String cmd) throws IOException {
        byte[] buffer;
        buffer = cmd.getBytes();
        sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
        datasocket.send(sPacket);
    }

    private static void sendTCPMessage(String cmd) throws IOException{
        outToServer.println(cmd);
    }

}

