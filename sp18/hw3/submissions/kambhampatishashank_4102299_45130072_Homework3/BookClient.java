import java.net.*;
import java.io.*;

public class BookClient {
    private String commandFilename;
    private boolean useUDP;
    private String clientId;
    private static final String SERVER_HOSTNAME = "localhost";
    private static final int TCP_PORT = 7000;
    private static final int UDP_PORT = 8000;

    public BookClient(String commandFilename, String clientId) {
        this.commandFilename = commandFilename;
        useUDP = true;
        this.clientId = clientId;
    }

    private String sendWithTCP(String msg) {
        try (Socket sock = new Socket(SERVER_HOSTNAME, TCP_PORT)) {
            DataOutputStream send = new DataOutputStream(sock.getOutputStream());
            send.writeBytes(clientId + " " + msg + "\n");

            BufferedReader receive = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            StringBuilder out = new StringBuilder();
            for(String line = receive.readLine(); line != null; line = receive.readLine()) {
                if(line.length() != 0) {
                    out.append(line);
                    out.append('\n');
                }
            }
            return out.toString();
        } catch(Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String sendWithUDP(String msg) {
        try {
            msg = clientId + " " + msg;
            InetAddress ia = InetAddress.getByName(SERVER_HOSTNAME);
            DatagramSocket datasocket = new DatagramSocket();

            byte[] buffer = msg.getBytes();
            DatagramPacket sPacket = new DatagramPacket(buffer, buffer.length, ia, UDP_PORT);
            datasocket.send(sPacket);

            byte[] rbuffer = new byte[10000];
            DatagramPacket rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            datasocket.receive(rPacket);
            String output = new String(rPacket.getData(), 0, rPacket.getLength());
            if(output.length() == 0) {
                return output;
            } else {
                return output + "\n";
            }
        } catch(Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void run() throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(commandFilename));
        BufferedWriter out = new BufferedWriter(new FileWriter("out_" + clientId + ".txt"));

        for(String line = in.readLine(); line != null; line = in.readLine()) {
            if(useUDP) {
                if(line.equals("setmode T")) {
                    useUDP = false;
                }
                out.write(sendWithUDP(line));
            } else {
                if(line.equals("setmode U")) {
                    useUDP = true;
                }
                out.write(sendWithTCP(line));
            }
        }

        in.close();
        out.close();
    }

    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("Usage: java BookClient <command-file> <client-id>");
            System.exit(0);
        }

        BookClient client = new BookClient(args[0], args[1]);

        try {
            client.run();
        } catch(IOException e) {
            System.err.println("Command file does not exist or no write permissions to folder");
            System.exit(1);
        }
    }
}
