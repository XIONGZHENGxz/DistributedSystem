import java.io.*:
import java.net.*;

public class Server_TCP extends Thread {
    ServerSocket sock;
    Socket sockClient;
    BufferedReader buff;
    PrintStream data;
    String input;

    public Server_TCP() {
        try {
            sock = new ServerSocket(7000);
        } catch(IOException e) {
            System.err.println("I/O Exception in serversocket creation");
        }
    }

    public void run() {
        while(1) {
            try { 
                sockClient = sock.accept();
                data = new PrintStream(sockClient.getOutputStream());
                buff = new BufferedReader(new InputStreamReader(sockClient.getInputStream()));
                int currInUse = 1;
                synchronized(this) {
                    while(currInUse) {
                        input = buff.readLine();
                        String[] commands = input.split(":"); 
                        String command = commands[0];
                        switch(command) {
                            case "borrow":
                                int newRecordID = BookServer.borrow(commands[1], commands[2]);
                                data.println("1"); 
                                if(newRecordID == -1) {
                                    data.println("Request Failed - Book not available");
                                } else {
                                    data.println("Your request has been approved, " + newRecordID + " " + commands[1] + " " + commands[2]);
                                }
                                break;
                            case "return":
                                int requestresp = BookServer.return(Integer.valueOf(commands[1]));
                                data.println("1"); 
                                if(requestresp == 1) {
                                    data.println(commands[1] + " is returned");
                                } else {
                                    data.println(commands[1] + " not found, no such borrow record");
                                }
                                break;
                            case "list":
                                data.println(BookServer.listLength(commands[1]));
                                data.print(BookServer.list(commands[1]));
                                break;
                            case "inventory":
                                data.println(BookServer.inventoryLength());
                                data.print(BookServer.inventory());
                                break;
                            case "exit":
                                data.println("0");
                                BookServer.inventoryOutput();
                                currInUse = 0;
                                break;
                        }
                    }
                }
            } catch(Exception e) {
                System.err.println(e);
            }
        }
    }
}