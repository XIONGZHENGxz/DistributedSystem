import java.net.*;
import java.io.IOException;

public class Server_UDP extends Thread {
    private DatagramSocket sock;

    public Server_UDP() {
        try {
            sock = new DatagramSocket(8000);
        } catch(IOException e) {
            System.err.println("I/O Exception in datagramsocket creation");
        }
    }

    public void run() {
        while(1) {
            try {
                byte[] bytes = new byte[4096];
                DatagramPacket newPack = new DatagramPacket(bytes, bytes.length);
                sock.receive(newPack);
                String output = "";
                String incoming = new String(newPack.getData(), 0, newPack.getLength());
                String[] commands = incoming.split(":");
                String command = commands[0];
                switch(command) {
                    case "borrow":
                        int newRecordID = BookServer.borrow(commands[1], commands[2]);
                        if(newRecordID == -1) {
                            output = "Request Failed - Book not available";
                        } else {
                            output = "Your request has been approved, " + newRecordID + " " + commands[1] + " " + commands[2];
                        }
                        output += "\n";
                        break;
                    case "return":
                        int requestresp = BookServer.return(Integer.valueOf(commands[1]));
                        if(requestresp == 1) {
                            output = commands[1] + " is returned";
                        } else {
                            output = commands[1] + " not found, no such borrow record";
                        }
                        output += "\n";
                        break;
                    case "list":
                        output = BookServer.list(commands[1]);
                        break;
                    case "inventory":
                        output = BookServer.inventory();
                        break;
                    case "exit":
                        BookServer.inventoryOutput();
                        break;
                }
                INetAddress newAddr = newPack.getAddress();
                int newPort = newPack.getPort();
                bytes = output.getBytes();
                DatagramPacket outputPack = new DatagramPacket(bytes, bytes.length, newAddr, newPort);
                sock.send(outputPack);
            } catch(Exception e){
                System.err.println(e);
            }
        }
    }
}