import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;

public class ClientManager {
    static Object lock = new Object();
    int udpPort = 7000;// hardcoded -- must match the server's udp port
    int tcpPort = 8000;// hardcoded -- must match the server's tcp port
    DatagramSocket clientSocket;
    Socket tcpSocket;
    InetAddress IPAddress;
    InputStream sendStream;
    InputStream receiveStream;
    InetAddress serverAddress;
    boolean udp;
    int port;

    public ClientManager(String s) {
        serverAddress = null;
        try {
            IPAddress = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (s.equals("T")) {
            udp = false;
            tcpConnect();
        } else {
            udp = true;
            udpConnect();
        }
    }

    private void udpConnect() {
        clientSocket = null;
        try {
            clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName("localhost");
            byte[] sendData;
            byte[] receiveData = new byte[1024];
            String sendMsg = "Start";
            sendData = sendMsg.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, udpPort);
            clientSocket.send(sendPacket);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            String newPort = new String(receivePacket.getData(), 0, receivePacket.getLength());
            port = Integer.parseInt(newPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void tcpConnect() {
        try {
            String sentence = "Start";
            Socket clientSocket = new Socket("localhost", 8000);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToServer.writeBytes(sentence + '\n');
            port = Integer.parseInt(inFromServer.readLine());
            System.out.println("FROM SERVER: " + port);
            clientSocket.close();
            System.out.println("TCP connected");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String tcpSend(String s) {
        try {
            String reply = "";
            Socket clientSocket = new Socket("localhost", 8000);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.writeBytes("" + port + '\n');
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            int check_port = Integer.parseInt(inFromServer.readLine());
            System.out.println("ports equal:" + (port == check_port));
            tcpSocket = new Socket("localhost", port);
            outToServer = new DataOutputStream(tcpSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            outToServer.writeBytes(s + '\n');
            int numLines = inFromServer.read();
            while (numLines > 0) {
                String line = inFromServer.readLine();
                reply = reply + line + '\n';
                numLines--;
            }
            System.out.println("FROM SERVER: " + reply);
            if(reply.equals("EXIT\n")) {
                System.out.println("Exit worked here");
                return "EXIT";
            }
            return reply;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

    public String udpSend(String message) {
        try {
            InetAddress IPAddress = InetAddress.getByName("localhost");
            String sendMsg = message;
            byte[] sendData = sendMsg.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            clientSocket.send(sendPacket);
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            String reply = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println(reply);
            if (reply.equals("EXIT")) {
                clientSocket.close();
                return "EXIT";
            }
            return reply;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

    public String send(String message) {
        if (udp) {
            return udpSend(message);
        } else {
            return tcpSend(message);
        }
    }

}
