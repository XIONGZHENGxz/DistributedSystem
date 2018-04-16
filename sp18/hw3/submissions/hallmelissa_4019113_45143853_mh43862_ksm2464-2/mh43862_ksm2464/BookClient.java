import java.util.Scanner;
import java.io.*;
import java.util.*;
import java.net.*;


public class BookClient {

  public static void main (String[] args) {
    String hostAddress;
    int tcpPort;
    int udpPort;
    String port;
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
    port = "U";

    try {
      PrintStream outfile = new PrintStream(new FileOutputStream("out_" + clientId + ".txt"));
      System.setOut(outfile);
    } catch (FileNotFoundException e){
      e.printStackTrace();
    }

    try {
        // TCP
        InetAddress ia = InetAddress.getByName(hostAddress);
        Socket tcp = new Socket(ia, tcpPort);
        PrintWriter out = new PrintWriter(tcp.getOutputStream());
        Scanner in = new Scanner(tcp.getInputStream());

        // UDP
        DatagramSocket udp = new DatagramSocket();

        // Input file
        Scanner sc = new Scanner(new FileReader(commandFile));

        boolean stop = false;

        while(sc.hasNextLine()&&(stop==false)) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");

          if (tokens[0].equals("setmode")) {
            // TODO: set the mode of communication for sending commands to the server
            port = tokens[1];
          }
          else if (tokens[0].equals("borrow")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
            if(port.equals("U")){
              stop = communicateUdp(ia, udp, cmd, udpPort);
            }else{
              stop = communicateTcp(cmd, out, in);
            }

          } else if (tokens[0].equals("return")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
            if(port.equals("U")){
              stop = communicateUdp(ia, udp, cmd, udpPort);
            }else{
              stop = communicateTcp(cmd, out, in);

            }
          } else if (tokens[0].equals("inventory")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
            if(port.equals("U")){
              stop = communicateUdp(ia, udp, cmd, udpPort);
            }else{
              stop = communicateTcp(cmd, out, in);
            }

          } else if (tokens[0].equals("list")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
            if(port.equals("U")){
              stop = communicateUdp(ia, udp, cmd, udpPort);
            }else{
              stop = communicateTcp(cmd, out, in);
            }

          } else if (tokens[0].equals("exit")) {
            // TODO: send appropriate command to the server
            // System.out.println("Exit time:" + cmd);
            if(port.equals("U")){
              stop = communicateUdp(ia, udp, cmd, udpPort);
            }else{
              stop = communicateTcp(cmd, out, in);
            }
          } else {
            System.out.println("ERROR: No such command");
          }
        }
        tcp.close();
        udp.close();   
    } catch (FileNotFoundException e) {
	      e.printStackTrace();
    } catch (UnknownHostException e) {
        System.err.println(e);
    } catch (SocketException e) {
        System.err.println(e);
    } catch (IOException e) {
        System.err.println(e);
    }
  }

  public static boolean communicateUdp(InetAddress addr, DatagramSocket dataSocket, String msg, int udpPort) {
      try{
        byte[] rbuffer;
        rbuffer = new byte[1024];

        byte[] out = msg.getBytes();
        DatagramPacket sPacket = new DatagramPacket(
          out,
          out.length,
          addr,
          udpPort);

        dataSocket.send(sPacket);

        DatagramPacket rPacket = new DatagramPacket(rbuffer, rbuffer.length);
        dataSocket.receive(rPacket);

        String retstring = new String(rPacket.getData(), 0,
                  rPacket.getLength());
        if(retstring.length()>=4 && retstring.substring(0,4).equals("exit")){
          PrintStream invFile = new PrintStream(new FileOutputStream("inventory.txt"));
          System.setOut(invFile);
          System.out.println(retstring.substring(5));
          return true;
        }
        System.out.print(retstring);

      }
      catch (UnknownHostException e) {
          System.err.println(e);
      } catch (SocketException e) {
          System.err.println(e);
      } catch (IOException e) {
          System.err.println(e);
      }

      return false;
  }

  public static boolean communicateTcp(String msg, PrintWriter out, Scanner in) {
    // Send
    out.println(msg);
    out.flush();

    // TODO: receive response

    boolean exit = false;
    while(true){
      while (in.hasNextLine() || in.hasNext()) {
        String response = in.nextLine();
        if(response.length() == 0){
          return exit;
        }
        if (exit == true || response.length()>=4 && response.substring(0,4).equals("exit")){
          try{
            PrintStream invFile = new PrintStream(new FileOutputStream("inventory.txt"));
            System.setOut(invFile);
            exit = true;
            while (in.hasNextLine() || in.hasNext()) {
              response = in.nextLine();  
              System.out.println(response);
              if(response.length() == 0){
                return exit;
              }
            }
          } catch (FileNotFoundException e){
            e.printStackTrace();
          }
        }
        System.out.println(response);
        
      }

      return exit;
    }

    // TODO: print response

  }
}
