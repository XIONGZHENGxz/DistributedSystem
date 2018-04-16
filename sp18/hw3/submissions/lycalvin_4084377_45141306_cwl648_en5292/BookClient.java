import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.io.*;
import java.util.*;
public class BookClient {
  private static final int tcpPort = 7000;// hardcoded -- must match the server's tcp port
  private static final int udpPort = 8000;// hardcoded -- must match the server's udp port
  private static int clientId;
  private static String hostAddress;
  public static void main (String[] args) {

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
        Scanner sc = new Scanner(new FileReader(commandFile));
        File file = new File("out_" + clientId + ".txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fr = new FileWriter(file);
        boolean useTCP = false;
        DatagramSocket udp_socket = new DatagramSocket();
        Socket tcp_socket = null;
        PrintWriter tcp_out = null;
        Scanner tcp_in = null;
        int port_num = Integer.parseInt(sendUDP("init", udp_socket, udpPort));
        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");

          if (tokens[0].equals("setmode")) {
            // TODO: set the mode of communication for sending commands to the server
            boolean new_mode = tokens[1].equals("T");
            if (new_mode != useTCP) {
                if (useTCP) { // Switching to UDP
                    port_num = Integer.parseInt(sendTCP("switching", tcp_out, tcp_in));
                    tcp_socket.close();
                    tcp_out.close();
                    tcp_in.close();
                    udp_socket = new DatagramSocket();
                } else { // Switching to TCP
                    port_num = Integer.parseInt(sendUDP("switching", udp_socket, port_num));
                    udp_socket.close();
                    tcp_socket = new Socket(hostAddress, port_num);
                    tcp_out = new PrintWriter(tcp_socket.getOutputStream());
                    tcp_in = new Scanner(tcp_socket.getInputStream());
                }
                useTCP = new_mode;
            }
          }
          else if (tokens[0].equals("borrow") || tokens[0].equals("return") ||
                  tokens[0].equals("inventory") || tokens[0].equals("list")) {
            String resp;
            if (useTCP) {
                resp = sendTCP(cmd, tcp_out, tcp_in) + "\n";
            } else {
                resp = sendUDP(cmd, udp_socket, port_num) + "\n";
            }
            fr.write(resp, 0, resp.length());
          } else if (tokens[0].equals("exit")) {
              if (useTCP) {
                  sendTCP(cmd, tcp_out, tcp_in);
              } else {
                  sendUDP(cmd, udp_socket, port_num);
              }
              break;
          } else {
            System.out.println("ERROR: No such command");
          }
        }
        fr.close();
        if (useTCP) {
            tcp_socket.close();
            tcp_out.close();
            tcp_in.close();
        } else {
            udp_socket.close();
        }
    } catch (FileNotFoundException e) {
	    e.printStackTrace();
    } catch (Exception e) {}
  }

  private static String sendTCP(String message, PrintWriter outToServer, Scanner inFromServer) {
      try {
          outToServer.println(message);
          outToServer.flush();
          String resp = inFromServer.nextLine();
          if ((message.startsWith("list") && Character.isDigit(resp.charAt(0))) || message.startsWith("inventory")) {
              ArrayList<String> responses = new ArrayList<>();
              int num_resp = Integer.parseInt(resp);
              for(int i = 0; i < num_resp; i++) {
                  responses.add(inFromServer.nextLine());
              }
              resp = String.join("\n", responses);
          }
          return resp;
      } catch (Exception e) {
          e.printStackTrace();
      }
      return "";
  }
  private static String sendUDP(String message, DatagramSocket datasocket, int port) {
      try {
          InetAddress ia = InetAddress.getByName(hostAddress);
          byte[] buffer = message.getBytes();
          DatagramPacket sPacket = new DatagramPacket(buffer, buffer.length, ia, port);
          datasocket.send(sPacket);
          byte[] rbuffer = new byte[1024];
          DatagramPacket rPacket = new DatagramPacket(rbuffer, rbuffer.length);
          datasocket.receive(rPacket);
          String resp = new String(rPacket.getData(), 0, rPacket.getLength());
          if ((message.startsWith("list") && Character.isDigit(resp.charAt(0))) || message.startsWith("inventory")) {
            Scanner sc = new Scanner(resp);
            int num = Integer.parseInt(sc.nextLine());
            ArrayList<String> responses = new ArrayList<>();
            for (int i = 0; i < num; i++) {
                responses.add(sc.nextLine());
            }
            resp = String.join("\n", responses);
          }
          return resp;
      } catch (Exception e) {}
      return "";
  }
}
