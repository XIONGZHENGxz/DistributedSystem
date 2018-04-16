//package hwk3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPClientHandler extends Thread {
    DatagramSocket socket;
    volatile boolean done;
    int port;
    final int len = 2048;
    public UDPClientHandler(DatagramSocket udpSocket, int port) {
        socket = udpSocket;
        done = false;
        this.port = port;
    }

    public void run(){
        // process all client requests until switch request
        while(!done) {
            try {
                byte[] rBuffer = new byte[len];
                DatagramPacket dataPacket = new DatagramPacket(rBuffer, rBuffer.length);
                socket.receive(dataPacket);

                String request = new String(dataPacket.getData(), 0, dataPacket.getLength());
                String[] tokens = request.split(" ");

                if (tokens[0].equals("setmode")) {
                    if (tokens[1].equals("T")) done = true;
                } else {
                    String returnString = BookServer.performCommand(tokens);
                    String[] items = returnString.split("(?<=\\n)"); //split after each newline, keeping new line character
                    byte[] line;
                    DatagramPacket returnMessage;
                    for(String s : items) {
                        line = s.getBytes();
                        returnMessage = new DatagramPacket(line, line.length, dataPacket.getAddress(), dataPacket.getPort());
                        socket.send(returnMessage);
                    }
                    byte[] endLine = "end".getBytes();
                    returnMessage = new DatagramPacket(endLine, endLine.length, dataPacket.getAddress(), dataPacket.getPort());
                    socket.send(returnMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

}
