import java.net.*;
import java.util.Scanner;
import java.io.*;

public class BookClient {

    private static DatagramSocket UDPSocket;
    private static final int MAX_LEN = 1024;
    private static boolean isTCP;
    private static String hostAddress = "localhost";
    private static int port;

    public static void main (String[] args) {
        if (args.length != 2) {
            System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
            System.out.println("\t(1) <command-file>: file with commands to the server");
            System.out.println("\t(2) client id: an integer between 1..9");
            System.exit(-1);
        }

        String commandFile = args[0];
        int clientId = Integer.parseInt(args[1]);

        try {
            PrintStream outFile = new PrintStream(new File("out_" + clientId + ".txt"));
            System.setOut(outFile);

            UDPSocket = new DatagramSocket();
            UDPSocket.send(new DatagramPacket(new byte[1],1, InetAddress.getByName(hostAddress), 8000));
            DatagramPacket packet = new DatagramPacket(new byte[1], 1);
            UDPSocket.receive(packet);
            port = packet.getPort();

            Socket TCPSocket = new Socket(hostAddress, 7000);
            Scanner reader = new Scanner(TCPSocket.getInputStream());
            PrintStream writer = new PrintStream(TCPSocket.getOutputStream(), true);

            Scanner sc = new Scanner(new FileReader(commandFile));

            while (sc.hasNextLine()) {
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");

                switch (tokens[0]) {
                    case "setmode":
                        if (isTCP)
                            writer.println(cmd);
                        else
                            udp(cmd, false);
                        isTCP = tokens[1].equals("T");
                        break;

                    case "borrow":
                    case "return":
                        if (isTCP) {
                            writer.println(cmd);
                            System.out.println(reader.nextLine());
                        }
                        else
                            udp(cmd, true);
                        break;

                    case "inventory":
                    case "list":
                        if (isTCP) {
                            writer.println(cmd);

                            int count = Integer.parseInt(reader.nextLine());
                            for (int i = 0; i < count; i++)
                                System.out.println(reader.nextLine());
                        } else
                            udp(cmd, true);
                        break;

                    case "exit":
                        if (isTCP)
                            writer.println(cmd);
                        else
                            udp(cmd, false);

                        reader.close();
                        writer.close();
                        TCPSocket.close();
                        UDPSocket.close();
                        break;

                    default:
                        System.out.println("ERROR: No such command");

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void udp(String cmd, boolean waitForServer) throws IOException {
        byte[] writebuf = cmd.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(writebuf, writebuf.length, InetAddress.getByName(hostAddress), port);
        UDPSocket.send(sendPacket);

        if (waitForServer) {
            byte[] readbuf = new byte[MAX_LEN];
            DatagramPacket receivePacket = new DatagramPacket(readbuf, readbuf.length);
            UDPSocket.receive(receivePacket);
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println(response);
        }
    }
}

