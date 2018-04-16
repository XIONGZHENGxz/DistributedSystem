import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.io.*;
import java.util.*;

public class BookClient {
    public static void main(String[] args) throws Exception {
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
        ClientManager myClient = null;
        File clientFile = new File("out_" + clientId + ".txt");
        PrintWriter printWriter = new PrintWriter(clientFile);
        printWriter.write("");
        printWriter.close();
        try {
            Scanner sc = new Scanner(new FileReader(commandFile));
            while (sc.hasNextLine()) {
                String cmd = sc.nextLine();
                System.out.println(cmd);
                String[] tokens = cmd.split(" ");

                if (tokens[0].equals("setmode")) {
                    System.out.println("setting mode");
                    if (tokens.length == 2) myClient = new ClientManager(tokens[1]);
                    else myClient = new ClientManager("U");
                } else {
                    if (myClient == null) myClient = new ClientManager("U");
                    String response = myClient.send(cmd);
                    PrintWriter writer = new PrintWriter(
                            new FileOutputStream(clientFile, true));
                    System.out.println("RESPONSE:" + response);
                    System.out.println(response.equals("EXIT"));
                    String[] lines = response.split("\r\n|\r|\n");
                    if (!response.equals("EXIT")) {
                        for (int i = 0; i < lines.length; i++) {
                            writer.println(lines[i]);
                            writer.flush();
                        }
                    }
                    else {
                        myClient = null;
                        writer.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}