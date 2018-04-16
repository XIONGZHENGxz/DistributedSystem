import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServerClientThread extends Thread{
    Socket tcpClient;

    DatagramSocket udpClient;
    DatagramPacket sendPacket, receivePacket;

    PrintWriter tcpSocketWriter = new PrintWriter(System.out);

    boolean isUsingUDP;


    public ServerClientThread(Socket s){
        this.tcpClient = s;
        this.isUsingUDP = false;
    }

    public ServerClientThread(DatagramSocket udpClient, DatagramPacket receivePacket){
        this.udpClient = udpClient;
        this.receivePacket = receivePacket;
        this.isUsingUDP = true;
    }

    public void sendData(String response){
        try {
            if(isUsingUDP){
                byte[] responseBuffer = response.getBytes();
                sendPacket = new DatagramPacket(responseBuffer, responseBuffer.length, receivePacket.getAddress(), receivePacket.getPort());
                udpClient.send(sendPacket);
                //System.out.println("Sent UDP Packet back to client");
            }
            else {
                tcpSocketWriter.println(response);
                tcpSocketWriter.flush();
                //System.out.println("Sent TCP Packet response to client");
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void run(){
        if(isUsingUDP){
            try {
                // do something with received packet and send back response
                String command = new String(receivePacket.getData(), 0, receivePacket.getLength());
                //System.out.println("Received from Client UDP: " + command);
                String[] tokens = command.split(" ");
                String response = "notFilled";
                if (tokens[0].equals("borrow")) {
                    String studentName = tokens[1];
                    tokens = command.split(" \"");
                    tokens[1] = "\"" + tokens[1];
                    String bookName = tokens[1];
                    response = BookServer.borrow(studentName, bookName);
                } else if (tokens[0].equals("return")) {
                    int recordId = Integer.parseInt(tokens[1]);
                    response = BookServer.returnRecordId(recordId);
                } else if (tokens[0].equals("inventory")) {
                    response = BookServer.inventory();
                } else if (tokens[0].equals("list")) {
                    String studentName = tokens[1];
                    response = BookServer.listRecords(studentName);
                }
                else if (tokens[0].equals("exit")) {
                    response = BookServer.inventory();
                    PrintWriter toFile = new PrintWriter("inventory.txt", "UTF-8");
                    String[] splitResponse = response.split("\n");
                    for(int i = 1; i < splitResponse.length; i++){
                        toFile.println(splitResponse[i]);
                    }
                    toFile.flush();
                    toFile.close();
                }
                sendData(response);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        else {
            try {
                //System.out.println("in tcp processing");
                Scanner scan = new Scanner(tcpClient.getInputStream());
                tcpSocketWriter = new PrintWriter(tcpClient.getOutputStream());
                while(scan.hasNextLine()){
                    String command = scan.nextLine();
                    //System.out.println("Received from Client TCP: " + command);
                    String[] tokens = command.split(" ");
                    String response = "notFilled";
                    if (tokens[0].equals("borrow")) {
                        String studentName = tokens[1];
                        tokens = command.split(" \"");
                        tokens[1] = "\"" + tokens[1];
                        String bookName = tokens[1];
                        //System.out.println(bookName);
                        response = BookServer.borrow(studentName, bookName);
                    } else if (tokens[0].equals("return")) {
                        int recordId = Integer.parseInt(tokens[1]);
                        response = BookServer.returnRecordId(recordId);
                    } else if (tokens[0].equals("inventory")) {
                        response = BookServer.inventory();
                    } else if (tokens[0].equals("list")) {
                        String studentName = tokens[1];
                        response = BookServer.listRecords(studentName);
                        //System.out.println(response);
                    }
                    else if (tokens[0].equals("exit")) {
                        //System.out.println("getting to print");
                        response = BookServer.inventory();
                        PrintWriter toFile = new PrintWriter("inventory.txt", "UTF-8");
                        String[] splitResponse = response.split("\n");
                        for(int i = 1; i < splitResponse.length; i++){
                            toFile.println(splitResponse[i]);
                        }
                        //toFile.println(response);
                        toFile.flush();
                        toFile.close();
                        break;
                    }
                    sendData(response);

//                    if(isUsingUDP){
//                        byte[] responseBuffer = response.getBytes();
//                        sendPacket = new DatagramPacket(responseBuffer, responseBuffer.length, receivePacket.getAddress(), receivePacket.getPort());
//                        udpClient.send(sendPacket);
//                        System.out.println("Sent UDP Packet, waiting on receive");
//                        responseBuffer = new byte[maxLength];
//                        receivePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
//                        datagramSocket.receive(receivePacket);
//                        String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
//                        System.out.println("Response from server UDP: " + response);
//                    }
//                    else {
//                        tcpSocketWriter.println(cmd);
//                        tcpSocketWriter.flush();
//                        String response = tcpSocketReader.nextLine();
//                        System.out.println("Response from server TCP: " + response);
//                    }
//
//                    pw.println(command);
//                    pw.flush();
                }
                System.out.flush();
                scan.close();
                tcpSocketWriter.flush();
                tcpSocketWriter.close();
                tcpClient.close();
            } catch (IOException e){
                System.out.println("errored");
                e.printStackTrace();
            }
        }
    }


}
