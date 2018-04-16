import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientHandler implements Runnable {
    private static final int MAX_LEN = 1024;
    private boolean isUDP = true;
    private int clientPort;
    private static InetAddress address;
    private Socket TCPSocket;
    private DatagramSocket UDPSocket;

    private Scanner reader;
    private PrintStream writer;

    static {
        try {
            address = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    ClientHandler(Socket TCPSocket, DatagramSocket UDPSocket, int clientPort) throws IOException {
        this.writer = new PrintStream(TCPSocket.getOutputStream(), true);
        this.reader = new Scanner(TCPSocket.getInputStream());
        this.TCPSocket = TCPSocket;
        this.UDPSocket = UDPSocket;
        this.clientPort = clientPort;
    }

    @Override
    public void run() {
        boolean exit = false;

        try {
            while (!exit) {

                String received = receive();
                Matcher matcher = Pattern.compile("([^\"]\\S*|\".+\")\\s*").matcher(received);
                matcher.find();
                switch (matcher.group(1)) {
                    case "setmode":
                        matcher.find();
                        String mode = matcher.group(1);
                        setMode(mode);
                        break;
                    case "borrow":
                        matcher.find();
                        String name = matcher.group(1);
                        matcher.find();
                        String book = matcher.group(1);
                        send(BookServer.borrowBook(name, book));
                        break;
                    case "return":
                        matcher.find();
                        int id = Integer.parseInt(matcher.group(1));
                        send(BookServer.returnBook(id));
                        break;
                    case "list":
                        matcher.find();
                        String studentName = matcher.group(1);
                        sendPayload(BookServer.list(studentName, isUDP));
                        break;
                    case "inventory":
                        sendPayload(BookServer.inventory(isUDP));
                        break;
                    case "exit":
                        BookServer.exit();
                        exit = true;
                        break;
                }

            }
            writer.close();
            reader.close();
            TCPSocket.close();
            UDPSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setMode(String mode) throws IOException {
        isUDP = mode.equals("U");
    }

    private String receive() throws IOException {
        String request;
        if (isUDP) {
            byte[] rbuf = new byte[MAX_LEN];
            DatagramPacket receivePacket = new DatagramPacket(rbuf, rbuf.length);
            UDPSocket.receive(receivePacket);
            request = new String(receivePacket.getData(), 0, receivePacket.getLength());
        } else {
            request = reader.nextLine();
        }

        return request;
    }

    private void send(String response) throws IOException {
        if (isUDP) {
            byte[] sbuf = response.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sbuf, sbuf.length, address, clientPort);
            UDPSocket.send(sendPacket);
        } else
            writer.println(response);
    }

    private void sendPayload(String[] responses) throws IOException {
        for (String response : responses)
            send(response);
    }

}
