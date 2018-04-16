import java.net.*;
import java.util.Scanner;
import java.io.*;
public class BookClient {
    static PrintWriter clientWriter;
    public static void main (String[] args) {

        String hostAddress;
        int tcpPort;
        int udpPort;
        int clientId;

        boolean udpMode = true;
        DatagramSocket udpSocket;
        Socket tcpSocket = null;

        Boolean beginning = true;
        Boolean initializeTCP = true;

        BufferedReader in = null;
        PrintWriter out = null;

        DatagramPacket sPacket = null;
        InetAddress ia;


        if (args.length != 2) {

            System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
            System.out.println("\t(1) <command-file>: file with commands to the server");
            System.out.println("\t(2) client id: an integer between 1..9");
            System.exit(-1);
        }

        String commandFile = args[0];
        clientId = Integer.parseInt(args[1]);
        hostAddress = "localhost";
        try {
            clientWriter = new PrintWriter("out_" + clientId + ".txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        tcpPort = 7000;// hardcoded -- must match the server's tcp port
        udpPort = 8000;// hardcoded -- must match the server's udp port

        try {
            Scanner sc = new Scanner(new FileReader(commandFile));

            udpSocket = new DatagramSocket();
            ia = InetAddress.getByName(hostAddress);


            while(sc.hasNextLine()) {

                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");

                if (tokens[0].equals("setmode")) {
                    if(beginning) {
                        beginning = false;
                        if (tokens[1].equals("T")) {
                            udpMode = false;
                            initializeTCP = true;
                        }
                    } else {
                        if(tokens[1].equals("T")) {
                            if(udpMode) {
                                initializeTCP = true;
                            }
                            udpMode = false;

                        } else {
                            if(tcpSocket != null) {
                                out.println("exit");
                                tcpSocket.close();
                                tcpSocket = null;
                            }
                            udpMode = true;
                        }
                    }
                    continue;
                }

                if(initializeTCP && !udpMode) {
                    tcpSocket = new Socket(hostAddress, tcpPort);
                    in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(tcpSocket.getOutputStream())), true);
                    initializeTCP = false;
                }
                else if(udpMode) {
                    in = null;
                    out = null;
                }

                if (tokens[0].equals("borrow")) {
                    // TODO: send appropriate command to the server and display the
                    // appropriate responses form the server

                    if(udpMode) {
                        udpComm(cmd, ia, udpPort, udpSocket);

                    } else {
                        tcpComm(cmd, in, out);
                    }

                } else if (tokens[0].equals("return")) {
                    // TODO: send appropriate command to the server and display the
                    // appropriate responses form the server
                    if(udpMode) {
                        udpComm(cmd, ia, udpPort, udpSocket);

                    } else {
                        tcpComm(cmd, in, out);

                    }

                } else if (tokens[0].equals("inventory")) {
                    // TODO: send appropriate command to the server and display the
                    // appropriate responses form the server
                    if(udpMode) {
                        udpComm(cmd, ia, udpPort, udpSocket);

                    } else {
                        out.println(cmd);
                        String str = in.readLine();
                        while(!str.equals("done")) {
                            System.out.println(str);
                            clientWriter.println(str);
                            str = in.readLine();
                        }
                    }

                } else if (tokens[0].equals("list")) {
                    // TODO: send appropriate command to the server and display the
                    // appropriate responses form the server
                    if(udpMode) {
                        udpComm(cmd, ia, udpPort, udpSocket);

                    } else {
                        tcpComm(cmd, in, out);

                    }

                } else if (tokens[0].equals("exit")) {
                    // TODO: send appropriate command to the server
                    if(udpMode) {
                        byte[] buffer = cmd.getBytes();
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ia, udpPort);
                        udpSocket.send(packet);
                    } else {
                        out.println("exit");
                    }
                    clientWriter.close();
                    break;

                } else {
                    System.out.println("ERROR: No such command");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /* communication between client and server through UDP */
    private static void udpComm(String cmd,
                                InetAddress ia,
                                int udpPort,
                                DatagramSocket udpSocket)
    {

        try {
            byte[] buffer = cmd.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ia, udpPort);
            udpSocket.send(packet);
            byte[] buf = new byte[1024];
            packet = new DatagramPacket(buf, buf.length);
            udpSocket.receive(packet);
            System.out.println(new String(packet.getData()));
            clientWriter.println(new String(packet.getData()).trim());
        }
        catch (IOException e) {}
    }


    /* communication between client and server through TCP */
    private static void tcpComm(String cmd, BufferedReader in, PrintWriter out)
    {

        try {
            out.println(cmd);
            String str = in.readLine();
            System.out.println(str);
            clientWriter.println(str);
        }
        catch (IOException e) {}
    }

}