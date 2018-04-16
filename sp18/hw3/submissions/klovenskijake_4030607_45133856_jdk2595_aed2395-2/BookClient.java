import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.io.*;
import java.util.*;
public class BookClient {



  public static void main (String[] args) {
    String hostAddress;
    int tcpPort;
    int udpPort;
    int clientId;
    Socket socket;
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

    try {
        Scanner sc = new Scanner(new FileReader(commandFile));
        InetAddress host = InetAddress.getByName(hostAddress);
        socket = new Socket(host,udpPort,false);
        PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");

          if (tokens[0].equals("setmode")) {
            socket.close();
            if(tokens[1].equals("T")) {
              socket = new Socket(host,tcpPort,true);
            }
            else if (tokens[1].equals("U")) {
              socket = new Socket(host,udpPort,false);
            }
            out = new PrintWriter(socket.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
          }
          else if (tokens[0].equals("borrow")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
            out.println(cmd);
            System.out.println(in.readLine());
          } else if (tokens[0].equals("return")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
            out.println(cmd);
            System.out.println(in.readLine());
          } else if (tokens[0].equals("inventory")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
            out.println(cmd);
            int numLines = Integer.parseInt(in.readLine());
            for(int i = 0; i < numLines; i++) {
              System.out.println(in.readLine());
            }
          } else if (tokens[0].equals("list")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
            out.println(cmd);
            int numLines = Integer.parseInt(in.readLine());
            for(int i = 0; i < numLines; i++) {
              System.out.println(in.readLine());
            }
          } else if (tokens[0].equals("exit")) {
            // TODO: send appropriate command to the server
            out.println(cmd);
          } else {
            System.out.println("ERROR: No such command");
          }
        }
    } catch (FileNotFoundException e) {
	e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
