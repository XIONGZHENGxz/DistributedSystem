import java.net.*;
import java.util.Scanner;
import java.io.*;
public class BookClient {
    public static void main (String[] args) {
        String hostAddress;
        int tcpPort;
        int udpPort;
        int currentPort;
        int clientId;

        if (args.length != 2) {
            System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
            System.out.println("\t(1) <command-file>: file with commands to the server");
            System.out.println("\t(2) client id: an integer between 1..9");
            System.exit(-1);
        }

        String commandFile = args[0];
        clientId = Integer.parseInt(args[1]);
        hostAddress = "localhost";

        tcpPort = 7000;// hardcoded -- must match the server's tcp port
        udpPort = 8000;// hardcoded -- must match the server's udp port

        BufferedWriter out = null;

        try{
            currentPort = udpPort;
            InetAddress ia = InetAddress.getByName(hostAddress);

            DatagramSocket dataSocket = new DatagramSocket(); // for udp
            Socket clientSocket = new Socket(hostAddress, tcpPort); // for tcp

            Scanner sc = new Scanner(new FileReader(commandFile));

            FileWriter fstream = new FileWriter("out_" + clientId + ".txt", false); //true tells to append data.
            out = new BufferedWriter(fstream);

            while(sc.hasNextLine()) {
                String cmd = sc.nextLine();
                cmd = cmd + " " + clientId;
                String[] tokens = cmd.split(" ");
                if (tokens[0].equals("setmode")) {
                    if(tokens[1].equals("T")){
                        currentPort = tcpPort;
                    }
                    if(tokens[1].equals("U")){
                        currentPort = udpPort;
                    }
                }
                else if (tokens[0].equals("borrow") || tokens[0].equals("return") || tokens[0].equals("inventory") || tokens[0].equals("list") || tokens[0].equals("exit")) {
                    if(currentPort == udpPort) {
                        byte[] retBufferUDP = new byte[4096];
                        byte[] sendBufferUDP = cmd.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendBufferUDP, sendBufferUDP.length, ia, udpPort);
                        dataSocket.send(sendPacket);
                        DatagramPacket recievePacket = new DatagramPacket(retBufferUDP, retBufferUDP.length);
                        if(!tokens[0].equals("exit")) {
                            dataSocket.receive(recievePacket);
                            String retString = new String(recievePacket.getData(), 0, recievePacket.getLength());
                            out.write(retString + "\n");
                        }
                    }
                    if(currentPort == tcpPort) {
                        char[] retBuffer = new char[4096];
                        BufferedReader retBufferTCP = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter sendBufferTCP = new PrintWriter(clientSocket.getOutputStream());
                        sendBufferTCP.println(cmd);
                        sendBufferTCP.flush();
                        if(!tokens[0].equals("exit")) {
                            retBufferTCP.read(retBuffer, 0, retBuffer.length);
                            out.write(new String(retBuffer));
                        }
                    }
                } else {
                    System.out.println("ERROR: No such command");
                }
            }

            clientSocket.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
