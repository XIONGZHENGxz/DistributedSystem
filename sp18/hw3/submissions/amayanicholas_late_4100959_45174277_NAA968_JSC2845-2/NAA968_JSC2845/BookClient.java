import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.io.*;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

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
        int baseUDPPort = 10000;

        try {
            Path p = Paths.get("./out_" + clientId + ".txt");
            Files.deleteIfExists(p);
            PrintWriter out = new PrintWriter(Files.newOutputStream(p, CREATE, APPEND), true);

            ClientNetworkConnector network =
                    new ClientNetworkConnector(
                            new InetSocketAddress("localhost", baseUDPPort + clientId),
                            new InetSocketAddress(hostAddress, udpPort));
            network.send("localhost," + String.valueOf(baseUDPPort + clientId));
            String[] sendAddress = network.receive().split(",");
            network.setUDPSendAddress(new InetSocketAddress(sendAddress[0], Integer.parseInt(sendAddress[1])));

            Scanner sc = new Scanner(new FileReader(commandFile));
            while(sc.hasNextLine()) {
                String cmd = sc.nextLine();
                String[] tokens = cmd.split("\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

                switch (tokens[0]) {
                    case "setmode":
                        setMode(network, tcpPort, tokens[1]);
                        break;
                    case "borrow":
                        borrow(out, network, tokens[1], tokens[2]);
                        break;
                    case "return":
                        returnBook(out, network, tokens[1]);
                        break;
                    case "inventory":
                        listInventory(out, network);
                        break;
                    case "list":
                        list(out, network, tokens[1]);
                        break;
                    case "exit":
                        exit(network);
                        break;
                    default:
                        System.out.println("ERROR: No such command");
                        break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            System.err.println("ERROR: Unknown host: " + hostAddress);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
        }
        System.out.println("Thread closing...");
    }

    private static void exit(ClientNetworkConnector network) throws IOException {
        network.send("exit");
        network.receive();
    }

    private static void setMode(ClientNetworkConnector network, int tcpPort, String mode) throws IOException {
        if (mode.equals("T") && !network.mode.equals(mode)) {
            network.send("setmode," + mode);
            System.out.println("1 " + network.receive().equals("tcp setup complete"));
            network.setMode(mode, new InetSocketAddress("localhost", tcpPort));
            network.send("tcp connection established");
            System.out.println("2 " + network.receive().equals("all clear"));
        } else if (mode.equals("U") && !network.mode.equals(mode)) {
            network.send("setmode," + mode);
            network.setMode(mode, null);
            String[] message = network.receive().split(",");
            network.setUDPSendAddress(new InetSocketAddress(message[0], Integer.parseInt(message[1])));
        }
    }

    private static void borrow(PrintWriter out, ClientNetworkConnector network, String studentName, String bookName) throws IOException {
        network.send("borrow," + studentName + "," + bookName);
        out.println(network.receive().replace("|", "\n"));
    }

    private static void returnBook(PrintWriter out, ClientNetworkConnector network, String studentName) throws IOException {
        network.send("return," + studentName);
        out.println(network.receive().replace("|", "\n"));
    }

    private static void list(PrintWriter out, ClientNetworkConnector network, String studentName) throws IOException {
        network.send("list," + studentName);
        out.println(network.receive().replace("|", "\n"));
    }

    private static void listInventory(PrintWriter out, ClientNetworkConnector network) throws IOException {
        network.send("inventory");
        out.println(network.receive().replace("|", "\n"));
    }



}
