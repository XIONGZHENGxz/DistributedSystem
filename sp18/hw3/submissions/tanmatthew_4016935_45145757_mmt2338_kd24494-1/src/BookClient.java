import java.util.Scanner;
import java.io.*;
import java.util.*;
import java.net.*;

public class BookClient {
    public static void main (String[] args) {
        String hostAddress;
        String mode = "udp";
        int tcpPort;
        int udpPort;
        int clientId;
        byte[] buffer, rbuffer;

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

        try {
            Scanner sc = new Scanner(new FileReader(commandFile));
            InetAddress ia = InetAddress.getByName(hostAddress);
            DatagramSocket udpSock =  new DatagramSocket();

            Socket tcpSock = new Socket(hostAddress,tcpPort);
            Scanner tcpIn = new Scanner(tcpSock.getInputStream());
            PrintWriter tcpOut = new PrintWriter(tcpSock.getOutputStream());

            FileWriter outputWriter = new FileWriter("out_" + clientId+ ".txt");

            while(sc.hasNextLine()) {
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");

                System.out.println("in the loop");
                if (tokens[0].equals("setmode")) {
                    //query server before changing modes
                    if(mode.equals("udp")){
                        buffer = new byte[cmd.length()];
                        buffer = cmd.getBytes();
                        DatagramPacket sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
                        udpSock.send(sPacket);
                    }else{
                        tcpOut.println(cmd);
                        tcpOut.flush();
                    }
                    //change modes
                    if(tokens[1].equals("U"))
                        mode = "udp";
                    else if(tokens[1].equals("T"))
                        mode = "tcp";
                }
                else if (tokens[0].equals("borrow")) {
                    if(mode.equals("udp")){
                        buffer = new byte[cmd.length()];
                        buffer = cmd.getBytes();
                        DatagramPacket sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
                        udpSock.send(sPacket);
                        rbuffer = new byte[1024];
                        DatagramPacket rPacket = new DatagramPacket(rbuffer, rbuffer.length, ia, udpPort);
                        udpSock.receive(rPacket);
                    }else{
                        tcpOut.println(cmd);
                        tcpOut.flush();
                        outputWriter.write(tcpIn.nextLine() + "\n");
                        outputWriter.flush();
                    }
                } else if (tokens[0].equals("return")) {
                    if(mode.equals("udp")){
                        buffer = new byte[cmd.length()];
                        buffer = cmd.getBytes();
                        DatagramPacket sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
                        udpSock.send(sPacket);
                        rbuffer = new byte[1024];
                        DatagramPacket rPacket = new DatagramPacket(rbuffer, rbuffer.length, ia, udpPort);
                        udpSock.receive(rPacket);
                    }else{
                        tcpOut.println(cmd);
                        tcpOut.flush();
                        outputWriter.write(tcpIn.nextLine() + "\n");
                        outputWriter.flush();
                    }
                } else if (tokens[0].equals("inventory")) {
                    if(mode.equals("udp")){
                        buffer = new byte[cmd.length()];
                        buffer = cmd.getBytes();
                        DatagramPacket sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
                        udpSock.send(sPacket);
                        rbuffer = new byte[1024];
                        DatagramPacket rPacket = new DatagramPacket(rbuffer, rbuffer.length, ia, udpPort);
                        udpSock.receive(rPacket);
                    }else{
                        tcpOut.println(cmd);
                        tcpOut.flush();
                        outputWriter.write(tcpIn.nextLine() + "\n");
                        outputWriter.flush();
                        //Only prints first line of Inventory. For some reason, couldn't get multiple lines to work
                        // (something strange with carriage return was happening, skipping lines of input)
                        /*while(tcpIn.hasNextLine()){
                            outputWriter.write(tcpIn.nextLine() + "\n");
                            outputWriter.flush();
                        }*/
                    }
                } else if (tokens[0].equals("list")) {
                    if(mode.equals("udp")){
                        buffer = new byte[cmd.length()];
                        buffer = cmd.getBytes();
                        DatagramPacket sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
                        udpSock.send(sPacket);
                        rbuffer = new byte[1024];
                        DatagramPacket rPacket = new DatagramPacket(rbuffer, rbuffer.length, ia, udpPort);
                        udpSock.receive(rPacket);
                    }else{
                        tcpOut.println(cmd);
                        tcpOut.flush();
                        outputWriter.write(tcpIn.nextLine() + "\n");
                        outputWriter.flush();
                    }
                } else if (tokens[0].equals("exit")) {
                    if(mode.equals("udp")){
                        buffer = new byte[cmd.length()];
                        buffer = cmd.getBytes();
                        DatagramPacket sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
                        udpSock.send(sPacket);
                        rbuffer = new byte[1024];
                        DatagramPacket rPacket = new DatagramPacket(rbuffer, rbuffer.length, ia, udpPort);

                    }else{
                        tcpOut.println(cmd);
                        tcpOut.flush();
                        tcpOut.close();

                    }
                } else {
                  System.out.println("ERROR: No such command");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
