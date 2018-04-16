/*
 * rsr873
 * vks377
 */

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class UDPThread extends Thread {
    private DatagramSocket datasocket;
    private DatagramPacket datapacket;
    private Library library;

    public UDPThread(DatagramSocket s, DatagramPacket p, Library l) {
        datasocket = s;
        datapacket = p;
        library = l;
    }

    public void run() {
        InetAddress sender = datapacket.getAddress();
        int senderPort = datapacket.getPort();
        String command = new String(datapacket.getData(), 0, datapacket.getLength());
        System.out.println("received: " + command);
        String[] cmdArray = command.split(" ");
        String tag = cmdArray[0].trim();
        if (tag.equals("borrow")) {
            String studentName = cmdArray[1].trim();
            String bookName = "";
            for(int i = 2; i < cmdArray.length - 1; i++)
                bookName += cmdArray[i].trim() + " ";
            bookName += cmdArray[cmdArray.length - 1].trim();
            int recordID = library.borrow(studentName, bookName);
            String response;
            if (recordID > 0)
                response = "Your request has been approved, " + recordID + " " + studentName + " " + bookName;
            else if(recordID == 0)
                response = "Request Failed - Book not available";
            else
                response = "Request Failed - We do not have this book";
            byte[] responseBytes = response.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(responseBytes, responseBytes.length, sender, senderPort);
            try {
                datasocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (tag.equals("return")) {
            int recordID = Integer.parseInt(cmdArray[1].trim());
            String bookName = library.returnBook(recordID);
            String response;
            if (!bookName.equals(""))
                response = recordID + " is returned";
            else
                response = recordID + " not found, no such borrow record";
            byte[] responseBytes = response.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(responseBytes, responseBytes.length, sender, senderPort);
            try {
                datasocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (tag.equals("list")) {
            String studentName = cmdArray[1].trim();
            String books = library.booksForStudent(studentName);
            String response;
            if (!books.equals(""))
                response = books;
            else
                response = 1 + "\n" + "No record found for " + studentName;
            byte[] responseBytes = response.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(responseBytes, responseBytes.length, sender, senderPort);
            try {
                datasocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (tag.equals("inventory")) {
            String response = library.getInventory();
            byte[] responseBytes = response.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(responseBytes, responseBytes.length, sender, senderPort);
            try {
                datasocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (tag.equals("exit")) {
            library.updateInventory();
        }
    }
}
