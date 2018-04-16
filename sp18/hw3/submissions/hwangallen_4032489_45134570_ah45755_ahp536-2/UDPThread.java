import java.io.IOException;
import java.net.*;

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
        String input = new String(datapacket.getData(), 0, datapacket.getLength());
        System.out.println("received: " + input);
        String[] tokens = input.split(" ");
        String command = tokens[0].trim();
        if (command.equals("borrow")) {
            String studentName = tokens[1].trim();
            String bookName = "";
            for(int i = 2; i < tokens.length - 1; i++)
                bookName += tokens[i]+ " ";
            bookName += tokens[tokens.length - 1]; //Format displays it as "Book Name" instead of "Book Name"_; String.trim does not seem to work.
            int recordID = library.borrowBook(studentName, bookName);
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
        } else if (command.equals("return")) {
            int recordID = Integer.parseInt(tokens[1].trim());
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
        } else if (command.equals("list")) {
            String studentName = tokens[1].trim();
            String books = library.listStudentBooks(studentName);
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
        } else if (command.equals("inventory")) {
            String response = library.getInventory();
            byte[] responseBytes = response.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(responseBytes, responseBytes.length, sender, senderPort);
            try {
                datasocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (command.equals("exit")) {
            library.updateInventory();
        }
    }
}