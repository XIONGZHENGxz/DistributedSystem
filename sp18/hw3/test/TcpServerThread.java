import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TcpServerThread extends Thread {
    private static final String MALFORMED_MESSAGE = "Error: Malformed message.";
    private BookServer bookServer;
    private Socket clientSocket;

    public TcpServerThread(BookServer bookServer, Socket client) {
        this.bookServer = bookServer;
        this.clientSocket = client;
    }

    @Override
    public void run() {
        Scanner scanner;
        String cmd; //client's request for server
        String[] tokens;
        String returnMessage; //server's message for client

        while(true) {
            try {
                scanner = new Scanner(clientSocket.getInputStream());
                cmd = scanner.nextLine();
                tokens = UdpServerThread.commandToArray(cmd);
                System.out.println("message from client recieved by server: " + cmd);

                switch (tokens[0]) {
//                    Expected: ['setmode', <'U'/'T'>] --> handled by clients
//                    case "setmode":

                    // Expected: ['borrow', <student name>, <book name>]
                    case "borrow":
                        if (tokens.length != 3) {
                            System.out.println(MALFORMED_MESSAGE);
                            break;
                        }
                        returnMessage = bookServer.borrow(tokens[1], tokens[2]) + " *message_end*";
                        sendMessage(returnMessage);
                        break;

                    // Expected: ['return', <record id>]
                    case "return":
                        if (tokens.length != 2) {
                            System.out.println(MALFORMED_MESSAGE);
                            break;
                        }
                        returnMessage = bookServer.returnBook(Integer.valueOf(tokens[1])) + " *message_end*";
                        sendMessage(returnMessage);
                        break;

                    // Expected: ['list', <student name>]
                    case "list":
                        if (tokens.length != 2) {
                            System.out.println(MALFORMED_MESSAGE);
                            break;
                        }
                        returnMessage = bookServer.list(tokens[1]) + " *message_end*";
                        sendMessage(returnMessage);
                        break;

                    // Expected: ['inventory']
                    case "inventory":
                        if (tokens.length != 1) {
                            System.out.println(MALFORMED_MESSAGE);
                            break;
                        }
                        returnMessage = bookServer.inventory() + " *message_end*";
                        sendMessage(returnMessage);
                        break;

                    // Expected: ['exit']
                    case "exit":
                        if (tokens.length != 1) {
                            System.out.println(MALFORMED_MESSAGE);
                            break;
                        }
                        returnMessage = bookServer.exit() + " *message_end*";
                        sendMessage(returnMessage);
                        return;

                    default:
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Sends message 'message' back to client
    private void sendMessage(String message) {
        try {
            PrintWriter pout = new PrintWriter(clientSocket.getOutputStream());
            System.out.println("sending: " + message + " to client");
            pout.println(message);
            pout.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
