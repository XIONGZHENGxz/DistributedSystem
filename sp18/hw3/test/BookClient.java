import java.net.*;
import java.util.Scanner;
import java.io.*;

public class BookClient {

    static DatagramPacket sendPacket, receivePacket;
    static boolean exitClient = false;

    private static String sendReceiveUDP(String cmd, String hostAddress, int udpPort){
        try{
            byte[] receiveBuffer = new byte[1024];
            byte[] buffer = new byte[1024];
            buffer = cmd.getBytes();
            DatagramSocket datasocket = new DatagramSocket();
            InetAddress ia = InetAddress.getByName(hostAddress);
            sendPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
            datasocket.send(sendPacket);
            receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            datasocket.receive(receivePacket);

            datasocket.close();

            return new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
        }catch (Exception e){
            e.printStackTrace();
            return " error ";
        }

    }

    private static String sendReceiveTCP(String cmd, String name, int tcpPort){
        String response = null;
        byte[] input = new byte[1024];

        try {

            Socket client = new Socket(name, tcpPort);
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            DataInputStream in = new DataInputStream(new BufferedInputStream((client.getInputStream())));
            out.println(cmd);
            in.read(input);
            response = new String(input);

            out.close();
            in.close();
            client.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return response.trim();
    }



    public static void main (String[] args){
        String hostAddress;
        int tcpPort;
        int udpPort;
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

        //UDP initialization

        String[] serverPorts = sendReceiveUDP("", hostAddress, udpPort).trim().split(" ");
        int udpServerPort = Integer.parseInt(serverPorts[0]);
        int tcpServerPort = Integer.parseInt(serverPorts[1]);

        Boolean tcpMode = false;   //default is UDP

        try {
            FileWriter ifw = new FileWriter("out_"+clientId+".txt", false);
            BufferedWriter ibw = new BufferedWriter(ifw);
            PrintWriter iout = new PrintWriter(ibw, true);
            iout.print("");
            iout.close();

            Scanner sc = new Scanner(new FileReader(commandFile));

            while(sc.hasNextLine() && !exitClient) {
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");
                DatagramPacket sendPacket, receivePacket;

                if(!tcpMode){
                    if (tokens[0].equals("setmode")) {

                        tcpMode = true;

                        if(tokens[1].trim().equals("T")){
                            tcpMode = true;
                            sendReceiveUDP(cmd, hostAddress, udpServerPort);
                        }else{
                            tcpMode = false;
                        }

                    }

                    else if (tokens[0].equals("borrow")) {

                        String response = sendReceiveUDP(cmd, hostAddress, udpServerPort);
                        System.out.println(response);

                        FileWriter fw = new FileWriter("out_"+clientId+".txt", true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter out = new PrintWriter(bw, true);
                        out.println(response);
                        out.close();


                    } else if (tokens[0].equals("return")) {

                        String response = sendReceiveUDP(cmd, hostAddress, udpServerPort);
                        System.out.println(response);

                        FileWriter fw = new FileWriter("out_"+clientId+".txt", true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter out = new PrintWriter(bw, true);
                        out.println(response);
                        out.close();


                    } else if (tokens[0].equals("inventory")) {

                        String response = sendReceiveUDP(cmd, hostAddress, udpServerPort);
                        System.out.println(response);

                        String[] inventory = response.split("\n");

                        FileWriter fw = new FileWriter("out_"+clientId+".txt", true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter out = new PrintWriter(bw, true);

                        for(String book : inventory){
                            out.println(book);
                        }

                        out.close();


                    } else if (tokens[0].equals("list")) {

                        String response = sendReceiveUDP(cmd, hostAddress, udpServerPort);
                        System.out.println(response);

                        String[] record = response.trim().split("\n");

                        FileWriter fw = new FileWriter("out_"+clientId+".txt", true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter out = new PrintWriter(bw, true);

                        for(String book : record){
                            out.println(book);
                        }
                        out.close();


                    } else if (tokens[0].equals("exit")) {

                        byte[] buffer = new byte[1024];
                        buffer = cmd.getBytes();
                        DatagramSocket datasocket = new DatagramSocket();
                        InetAddress ia = InetAddress.getByName(hostAddress);
                        sendPacket = new DatagramPacket(buffer, buffer.length, ia, udpServerPort);
                        datasocket.send(sendPacket);
                        exitClient = true;
                        datasocket.close();



                    } else {
                        System.out.println("ERROR: No such command");
                    }
                }else{
                    if (tokens[0].equals("setmode")) {

                        tcpMode = true;

                        if(tokens[1].trim().equals("T")){
                            tcpMode = true;
                        }else{
                            tcpMode = false;
                            sendReceiveTCP(cmd, hostAddress, tcpServerPort);
                        }

                    }
                    else if (tokens[0].equals("borrow")) {

                        String response = sendReceiveTCP(cmd, hostAddress, tcpServerPort);
                        System.out.println(response);

                        FileWriter fw = new FileWriter("out_"+clientId+".txt", true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter out = new PrintWriter(bw, true);
                        out.println(response);
                        out.close();


                    } else if (tokens[0].equals("return")) {

                        String response = sendReceiveTCP(cmd, hostAddress, tcpServerPort);
                        System.out.println(response);

                        FileWriter fw = new FileWriter("out_"+clientId+".txt", true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter out = new PrintWriter(bw, true);
                        out.println(response);
                        out.close();


                    } else if (tokens[0].equals("inventory")) {

                        String response = sendReceiveTCP(cmd, hostAddress, tcpServerPort);
                        System.out.println(response);

                        String[] inventory = response.split("\n");

                        FileWriter fw = new FileWriter("out_"+clientId+".txt", true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter out = new PrintWriter(bw, true);

                        for(String book : inventory){
                            out.println(book);
                        }

                        out.close();


                    } else if (tokens[0].equals("list")) {

                        String response = sendReceiveTCP(cmd, hostAddress, tcpServerPort);
                        System.out.println(response);

                        String[] record = response.trim().split("\n");

                        FileWriter fw = new FileWriter("out_"+clientId+".txt", true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter out = new PrintWriter(bw, true);

                        for(String book : record){
                            out.println(book);
                        }
                        out.close();

                    } else if (tokens[0].equals("exit")) {

                        Socket client = new Socket(hostAddress, tcpServerPort);
                        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                        out.println(cmd);

                        exitClient = true;
                        out.close();
                        client.close();


                    } else {
                        System.out.println("ERROR: No such command");
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}