import java.net.*;
import java.util.Scanner;
import java.io.*;
public class BookClient {

    public static void main (String[] args) {
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

        int maxLength = 1024;
        byte[] responseBuffer;
        boolean isUsingUDP = true;
        DatagramPacket sendPacket, receivePacket;

        String filename = "out_" + clientId + ".txt";

        try {
            Scanner sc = new Scanner(new FileReader(commandFile));
            DatagramSocket datagramSocket = new DatagramSocket();
            Socket tcpSocket = new Socket();
            Scanner tcpSocketReader = new Scanner(System.in);
            PrintWriter tcpSocketWriter = new PrintWriter(System.out);
            InetAddress ia = InetAddress.getByName("localhost");
            PrintWriter fileWriter = new PrintWriter(filename, "UTF-8");

            while(sc.hasNextLine()) {
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");

                //System.out.println("Parsing Command: " + cmd);

                if (tokens[0].equals("setmode")) {
                    // TODO: set the mode of communication for sending commands to the server
                    if(tokens[1].equals("T") && isUsingUDP){
                        isUsingUDP = false;
                        tcpSocket = new Socket("localhost", tcpPort);
                        tcpSocketReader = new Scanner(tcpSocket.getInputStream());
                        tcpSocketWriter = new PrintWriter(tcpSocket.getOutputStream());
                        datagramSocket.close();
                    }
                    else if (tokens[1].equals("U") && !isUsingUDP){
                        isUsingUDP = true;
                        datagramSocket = new DatagramSocket();
                        tcpSocketReader.close();
                        tcpSocketWriter.flush();
                        tcpSocketWriter.close();
                        tcpSocket.close();
                    }
                }
//                else if (tokens[0].equals("borrow")) {
//                    // TODO: send appropriate command to the server and display the
//                    // appropriate responses form the server
//
//
//                } else if (tokens[0].equals("return")) {
//                    // TODO: send appropriate command to the server and display the
//                    // appropriate responses form the server
//                } else if (tokens[0].equals("inventory")) {
//                    // TODO: send appropriate command to the server and display the
//                    // appropriate responses form the server
//                } else if (tokens[0].equals("list")) {
//                    // TODO: send appropriate command to the server and display the
//                    // appropriate responses form the server
//                }
                else if (tokens[0].equals("exit")) {
                    // TODO: send appropriate command to the server
                    if(isUsingUDP){
                        byte[] cmdBuffer = cmd.getBytes();
                        sendPacket = new DatagramPacket(cmdBuffer, cmdBuffer.length, ia, udpPort);
                        datagramSocket.send(sendPacket);
                        datagramSocket.close();
                    }
                    else {
                        tcpSocketWriter.println(cmd);
                        tcpSocketWriter.flush();
                        tcpSocketReader.close();
                        tcpSocketWriter.close();
                        tcpSocket.close();
                    }
                    break;
                } else {
                    if(isUsingUDP){
                        byte[] cmdBuffer = cmd.getBytes();
                        sendPacket = new DatagramPacket(cmdBuffer, cmdBuffer.length, ia, udpPort);
                        datagramSocket.send(sendPacket);
                        //System.out.println("Send UDP Packet, waiting on receive");
                        responseBuffer = new byte[maxLength];
                        receivePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
                        datagramSocket.receive(receivePacket);
                        String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        String[] splitResponse = response.split("\n");
                        if(isInteger(splitResponse[0])){
                            for(int i = 1; i < splitResponse.length; i++){
                                fileWriter.println(splitResponse[i]);
                            }
                            fileWriter.flush();
                        }
                        else {
                            fileWriter.println(response);
                            fileWriter.flush();
                        }
                    }
                    else {
                        tcpSocketWriter.println(cmd);
                        tcpSocketWriter.flush();
                        String response = tcpSocketReader.nextLine();
                        if(isInteger(response)){
                            for(int i = 0; i < Integer.parseInt(response); i++){
                                fileWriter.println(tcpSocketReader.nextLine());
                            }
                            fileWriter.flush();
                        }
                        else {
                            fileWriter.println(response);
                            fileWriter.flush();
                        }
                    }
                }
            }
            fileWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SocketException se) {
            se.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        } finally {
        }
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }
}