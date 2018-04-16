package hw3;

import java.net.*;
import java.util.Scanner;
import java.io.*;
public class BookClient {
    Scanner din;
    PrintStream pout;
    Socket tcpServer;
    InetAddress inetAddress;
    DatagramSocket udpServer;
    DatagramPacket sPacket, rPacket;
    int len = 65507;
    byte[] rbuffer = new byte[len];


    public void getTCPSocket(String hostAddress, int tcpPort) throws IOException {
        tcpServer = new Socket(hostAddress, tcpPort);
        din = new Scanner(tcpServer.getInputStream());
        pout = new PrintStream (tcpServer.getOutputStream());
    }

    public void getUDPSocket(String hostAddress) {
        try {
            inetAddress = InetAddress.getByName(hostAddress);
            udpServer = new DatagramSocket();
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
    }

    public void borrow(boolean isTCP, PrintWriter out, String hostAddress, int tcpPort, int udpPort, String studentName, String bookName) throws IOException {
        String message = "borrow_%_" + studentName + "_%_" + bookName + "_%_";
        if (isTCP) {
            pout.println(message);
            pout.flush();
            String retValue = din.nextLine();
            out.println(retValue);
            out.flush();
        } else {
            byte[] buffer = message.getBytes();
            sPacket = new DatagramPacket(buffer, buffer.length, inetAddress, udpPort);
            udpServer.send(sPacket);
            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            udpServer.receive(rPacket);
            String retValue = new String (rPacket.getData(),0, rPacket.getLength());
            out.println(retValue);
            out.flush();
        }
    }

    public void returnBook(boolean isTCP, PrintWriter out, String hostAddress, int tcpPort, int udpPort, String recordID) throws IOException {
        String message = "return_%_" + recordID + "_%_";
        if (isTCP) {
            pout.println(message);
            pout.flush();
            String retValue = din.nextLine();
            out.println(retValue);
            out.flush();
        } else {
            byte[] buffer = message.getBytes();
            sPacket = new DatagramPacket(buffer, buffer.length, inetAddress, udpPort);
            udpServer.send(sPacket);
            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            udpServer.receive(rPacket);
            String retValue = new String (rPacket.getData(),0, rPacket.getLength());
            out.println(retValue);
            out.flush();
        }
    }

    public void list(boolean isTCP, PrintWriter out, String hostAddress, int tcpPort, int udpPort, String studentName) throws IOException {
        String message = "list_%_" + studentName + "_%_";
        if (isTCP) {
            pout.println(message);
            pout.flush();
            String retValue = din.nextLine();
            String[] split = retValue.split("_%_");
            for (String s : split) {
                out.println(s);
            }
            out.flush();
        } else {
            byte[] buffer = message.getBytes();
            sPacket = new DatagramPacket(buffer, buffer.length, inetAddress, udpPort);
            udpServer.send(sPacket);
            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            udpServer.receive(rPacket);
            String retValue = new String (rPacket.getData(),0, rPacket.getLength());
            String[] split = retValue.split("_");
            for (String s : split) {
                out.println(s);
            }
            out.flush();
        }
    }

    public void inventory(boolean isTCP, PrintWriter out, String hostAddress, int tcpPort, int udpPort) throws IOException {
        String message = "inventory";
        if (isTCP) {
            pout.println(message);
            pout.flush();
            String retValue = din.nextLine();
            String[] split = retValue.split("_%_");
            for (String s : split) {
                out.println(s);
            }
            out.flush();
        } else {
            byte[] buffer = message.getBytes();
            sPacket = new DatagramPacket(buffer, buffer.length, inetAddress, udpPort);
            udpServer.send(sPacket);
            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            udpServer.receive(rPacket);
            String retValue = new String (rPacket.getData(),0, rPacket.getLength());
            String[] split = retValue.split("_%_");
            for (String s : split) {
                out.println(s);
            }
            out.flush();
        }
    }

    public void exit(boolean isTCP, String hostAddress, int tcpPort, int udpPort) throws IOException {
        String message = "exit";
        if (isTCP) {
            pout.println(message);
            pout.flush();
        } else {
            byte[] buffer = message.getBytes();
            sPacket = new DatagramPacket(buffer, buffer.length, inetAddress, udpPort);
            udpServer.send(sPacket);
        }
    }

    public static void main (String[] args) {
        String hostAddress;
        int tcpPort;
        int udpPort;
        int clientId;
        boolean isTCP = false;
        boolean access = true;
        PrintWriter poutFile;

        if (args.length != 2) {
            System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
            System.out.println("\t(1) <command-file>: file with commands to the tcpServer");
            System.out.println("\t(2) client id: an integer between 1..9");
            System.exit(-1);
        }


        String commandFile = args[0];
        clientId = Integer.parseInt(args[1]);
        hostAddress = "localhost";
        tcpPort = 7000;// hardcoded -- must match the tcpServer's tcp port
        udpPort = 8000;// hardcoded -- must match the tcpServer's udp port


        try {

            // hookup output file
            String fileName = "out_" + clientId + ".txt";
            File file = new File(fileName);
            file.createNewFile(); // if file already exists will do nothing
            poutFile =  new PrintWriter(new FileWriter(file));

            // start client code
            BookClient myClient = new BookClient();
            Scanner sc = new Scanner(new FileReader(commandFile));

            // hook up sockets
            myClient.getUDPSocket(hostAddress);
            myClient.getTCPSocket(hostAddress, tcpPort);

            while(sc.hasNextLine() && access) {
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");

                if (tokens[0].equals("setmode")) {
                    // TODO: set the mode of communication for sending commands to the tcpServer
                    isTCP = tokens[1].equals("T");
                }
                else if (tokens[0].equals("borrow")) {
                    // TODO: send appropriate command to the tcpServer and display the
                    // appropriate responses form the tcpServer
                    String name = "";
                    for (int i = 2; i < tokens.length - 1; i++) {
                        name += (tokens[i] + " ");
                    }
                    name += tokens[tokens.length - 1];

                    myClient.borrow(isTCP, poutFile, hostAddress, tcpPort, udpPort, tokens[1], name);
                } else if (tokens[0].equals("return")) {
                    // TODO: send appropriate command to the tcpServer and display the
                    // appropriate responses form the tcpServer
                    myClient.returnBook(isTCP, poutFile, hostAddress, tcpPort, udpPort, tokens[1]);
                } else if (tokens[0].equals("inventory")) {
                    // TODO: send appropriate command to the tcpServer and display the
                    // appropriate responses form the tcpServer
                    myClient.inventory(isTCP, poutFile, hostAddress, tcpPort, udpPort);
                } else if (tokens[0].equals("list")) {
                    // TODO: send appropriate command to the tcpServer and display the
                    // appropriate responses form the tcpServer
                    myClient.list(isTCP, poutFile, hostAddress, tcpPort, udpPort, tokens[1]);
                } else if (tokens[0].equals("exit")) {
                    // TODO: send appropriate command to the tcpServer
                    myClient.exit(isTCP, hostAddress, tcpPort, udpPort);
                    access = false;
                } else {
                    System.out.println("ERROR: No such command");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
