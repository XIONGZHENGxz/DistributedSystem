package hw3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ServerUDPThread extends Thread {
    DatagramSocket theClient;
    DatagramPacket dataPacket;
    Library library;
    int len = 65507;

    public ServerUDPThread(int port, Library library) {
        try {
            theClient = new DatagramSocket(port);
            this.library = library;
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        byte [] buf = new byte[len];
        while (true) {
            try {
                dataPacket = new DatagramPacket(buf, buf.length);
                theClient.receive(dataPacket);
                udpProcessThread t = new udpProcessThread(dataPacket, theClient, library);
                t.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public class udpProcessThread extends Thread {
        DatagramSocket theClient;
        DatagramPacket dataPacket;
        Library library;

        public udpProcessThread(DatagramPacket dataPacket, DatagramSocket s, Library library) {
            this.dataPacket = dataPacket;
            theClient = s;
            this.library = library;
        }
        @Override
        public void run() {
            String command = new String(dataPacket.getData(), 0, dataPacket.getLength());
            String[] split = command.split("_%_");

            if (split[0].equals("borrow")) {
                String response = library.borrow(split[1], split[2]);
                DatagramPacket returnPacket = new DatagramPacket(
                        response.getBytes(),
                        response.getBytes().length,
                        dataPacket.getAddress(),
                        dataPacket.getPort());
                try {
                    theClient.send(returnPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (split[0].equals("return")) {
                if (library.returnBook(Integer.parseInt(split[1]))) {
                    String response = Integer.parseInt(split[1]) + " is returned";
                    DatagramPacket returnPacket = new DatagramPacket(
                            response.getBytes(),
                            response.getBytes().length,
                            dataPacket.getAddress(),
                            dataPacket.getPort());
                    try {
                        theClient.send(returnPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    String response = Integer.parseInt(split[1]) + " not found, no such borrow record";
                    DatagramPacket returnPacket = new DatagramPacket(
                            response.getBytes(),
                            response.getBytes().length,
                            dataPacket.getAddress(),
                            dataPacket.getPort());
                    try {
                        theClient.send(returnPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (split[0].equals("list")) {
                String response = library.list(split[1]);
                DatagramPacket returnPacket = new DatagramPacket(
                        response.getBytes(),
                        response.getBytes().length,
                        dataPacket.getAddress(),
                        dataPacket.getPort());
                try {
                    theClient.send(returnPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (split[0].equals("inventory")) {
                String response = library.getInventory();
                DatagramPacket returnPacket = new DatagramPacket(
                        response.getBytes(),
                        response.getBytes().length,
                        dataPacket.getAddress(),
                        dataPacket.getPort());
                try {
                    theClient.send(returnPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (split[0].equals("exit")) {
                library.updateInventory();
            }
        }
    }

}