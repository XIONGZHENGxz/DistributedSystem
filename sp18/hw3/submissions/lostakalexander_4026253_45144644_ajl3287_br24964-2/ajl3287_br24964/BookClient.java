//eid_1: ajl3287
//eid_2: br24964
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class BookClient implements Runnable {
    private static final int DEFAULT_TCP_PORT = 7000;
    private static final int DEFAULT_UDP_PORT = 8000;
    private static final String UDP = "U";
    private static final String TCP = "T";
    private static final String DEFAULT_HOST = "localhost";

    private ArrayList<String> commands;
    private InetAddress host;
    private String protocol;
    private BufferedWriter fileWriter;

    private Socket tcpSock;
    private DataOutputStream tcpOut;
    private BufferedReader tcpIn;

    // UDP
    private int udpPort;
    private DatagramSocket udpSock;

    private BookClient(String fileName, int id) throws IOException {
        parseCommandFile(fileName);

        protocol = UDP;
        host = InetAddress.getByName(DEFAULT_HOST);
        udpPort = DEFAULT_UDP_PORT;
        File outFile = new File("out_" + id + ".txt");
        if (outFile.exists() && !outFile.delete())
            throw new RuntimeException("Old out file couldn't be deleted");
        fileWriter = new BufferedWriter(new FileWriter(outFile));

        // TCP setup
        tcpSock = new Socket(host, DEFAULT_TCP_PORT);
        tcpOut = new DataOutputStream(tcpSock.getOutputStream());
        tcpIn = new BufferedReader(new InputStreamReader(tcpSock.getInputStream()));

        // UDP setup
        udpSock = new DatagramSocket();
    }

    private void parseCommandFile(String fileName) throws IOException {
        commands = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        while ((line = reader.readLine()) != null)
            commands.add(line);
    }

    private void sendMessage(String message) throws IOException {
        switch (protocol) {
            case UDP:
                byte[] buf = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, host, udpPort);
                udpSock.send(packet);
                break;
            case TCP:
                tcpOut.writeBytes(message + '\n');
                break;
            default:
                System.err.println("Invalid protocol");
                System.exit(1);
        }
    }

    private String receiveMessage() {
        try {
            String message = null;
            switch (protocol) {
                case UDP:
                    final int BUF_SIZE = 4096;
                    final byte[] buf = new byte[BUF_SIZE];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    udpSock.receive(packet);
                    message = new String(packet.getData(), 0, packet.getLength());
                    break;
                case TCP:
                    StringBuilder builder = new StringBuilder();
                    while (!(message = tcpIn.readLine()).equals(""))
                        builder.append(message).append('\n');
                    if (builder.length() > 0)
                        builder.deleteCharAt(builder.length() - 1);
                    message = builder.toString();
                    break;
                default:
                    System.err.println("Invalid protocol");
                    System.exit(1);
            }
            return message;
        } catch (IOException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Invalid number of arguments");
            System.exit(1);
        }

        try {
            String commandFile = args[0];
            int id = Integer.parseInt(args[1]);

            new BookClient(commandFile, id).run();
        } catch (NumberFormatException e) {
            System.err.println("Invalid client ID");
            System.exit(1);
        } catch (Exception e) {
            System.err.println(e.toString());
            System.exit(1);
        }
    }

    @Override
    public void run() {
        try {
            for (String command : commands) {
                String[] commandSplit = command.split(" ");
                if (commandSplit[0].equals("setmode")) {
                    protocol = commandSplit[1];
                    continue;
                }

                try {
                    sendMessage(command);
                    if (!command.equals("exit")) {
                        fileWriter.write(receiveMessage() + "\n");
                    }
                } catch (IOException e) {
                    System.err.println("IOException: Sending command `" + command + "` failed");
                    break;
                }
            }
            tcpSock.close();
            fileWriter.close();
        } catch (IOException e) {
            System.err.println("Unexpected error occurred while sending message");
        }
    }
}
