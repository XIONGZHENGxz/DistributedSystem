// Robert Streit rps945
// Shyam Sabhaya sjs4367

import java.util.Scanner;
import java.io.*;
import java.util.*;
import java.net.*;

public class BookClient {
    private static boolean mode; //false - udp true - tcp
    private static String hostAddress;
    private static int tcpPort;
    private static int udpPort;
    private static int Userver;
    private static int cPort;
    private static int clientId;
    private static Scanner din ;
    private static PrintStream pout ;
    private static Socket server ;
    private static DatagramSocket datasocket;
    private static PrintStream pw;


    public static void initUDP()
    {
      try{

        InetAddress ia = InetAddress.getByName(hostAddress);
        String cmd = "init";
        int len = 500;
        byte[] rbuffer = new byte[len];
        DatagramPacket sPacket, rPacket;
        String reply = null;

        byte[] buffer = new byte[cmd.length()];
        buffer = cmd.getBytes();
        datasocket = new DatagramSocket();

        sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
        datasocket.send(sPacket);

        rPacket = new DatagramPacket(rbuffer, rbuffer.length);
        datasocket.receive(rPacket);
        reply = new String(rPacket.getData(), 0,rPacket.getLength());
        Userver = Integer.parseInt(reply);
        //System.err.println(datasocket.getLocalPort());

        //datasocket.setSendBufferSize(100000);
        //datasocket.setReceiveBufferSize(100000);
      }
      catch(Exception e)
      {
        e.printStackTrace();
        return;
      }
    }

    public static void initTCP()
    {
      InetAddress ia;

      try {
          ia = InetAddress.getByName(hostAddress);
          server = new Socket (ia,tcpPort);
          din = new Scanner(server.getInputStream());
          pout = new PrintStream(server.getOutputStream());
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
    }

    public static void closeUDP()
    {
      if(datasocket != null)
        datasocket.close();
      datasocket = null;
    }

    public static void closeTCP()
    {
      if(server!=null)
      {
        try{
        server.close();
        pout.close();
        din.close();
        server = null;
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
      }
    }

    public static void sendTCP(String cmd, String[] tokens)
    {
      //initTCP();
      switch(tokens[0])
      {
        case "setmode":
          if(tokens[1].equals("U"))
          {
            mode = false;
            pout.println(cmd);
            pout.flush();
            initUDP();
            closeTCP();
          }
          return;
        case "borrow":
        pout.println(cmd);
          break;

        case "return":
        pout.println(cmd);
          break;

        case "inventory":
        pout.println(cmd);
          break;

        case "list":
        pout.println(cmd);
          break;

        case "exit":
        pout.println(cmd);
        pout.flush();
        closeTCP();
        System.exit(0);
        return;

        default:
          System.out.println("ERROR: No such command");
          return;
      }
      pout.flush();
      String reply = din.nextLine();
      System.out.println(reply.replaceAll("@","\n"));
      //closeTCP();
    }

    public static void sendUDP(String cmd, String[] tokens)
    {
      InetAddress ia;
      try{
        ia = InetAddress.getByName(hostAddress);
      }
      catch(Exception e)
      {
        e.printStackTrace();
        return;
      }

      int len = 10000;

      byte[] rbuffer = new byte[len];
      DatagramPacket sPacket, rPacket;
      //String reply = null;

      byte[] buffer = new byte[cmd.length()];
      buffer = cmd.getBytes();
      String reply = "RESEND";
      //System.err.println(cmd);
      //while(reply.equals("RESEND"))
      //{
        try{
        switch(tokens[0])
        {
          case "setmode":
            if(tokens[1].equals("T"))
            {
              mode = true;
              sPacket = new DatagramPacket(buffer,buffer.length,ia,Userver);
              datasocket.send(sPacket);
              initTCP();
              closeUDP();
              return;
            }
            else
            {
              return;
            }
          case "borrow":
            sPacket = new DatagramPacket(buffer, buffer.length, ia, Userver);
            datasocket.send(sPacket);

            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            datasocket.receive(rPacket);
            reply = new String(rPacket.getData(), 0,rPacket.getLength());
            break;

          case "return":
            sPacket = new DatagramPacket(buffer, buffer.length, ia, Userver);
            datasocket.send(sPacket);

            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            datasocket.receive(rPacket);
            reply = new String(rPacket.getData(), 0,rPacket.getLength());
            break;

          case "inventory":
            sPacket = new DatagramPacket(buffer, buffer.length, ia, Userver);
            datasocket.send(sPacket);

            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            datasocket.receive(rPacket);
            reply = new String(rPacket.getData(), 0,rPacket.getLength());
            break;

          case "list":
            sPacket = new DatagramPacket(buffer, buffer.length, ia, Userver);
            datasocket.send(sPacket);

            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            datasocket.receive(rPacket);
            reply = new String(rPacket.getData(), 0,rPacket.getLength());
            break;

          case "exit":
            sPacket = new DatagramPacket(buffer, buffer.length, ia, Userver);
            datasocket.send(sPacket);
            closeUDP();
            System.exit(0);
            break;

          default:
            System.out.println("ERROR: No such command");
            break;
        }
      }
        catch(Exception e)
        {
          e.printStackTrace();
        }

    //}

      if(reply!=null)
      {
        System.out.println(reply.replaceAll("@","\n"));
      }
      //System.err.println(datasocket.getPort());
      //closeUDP();
  }

  public static void main (String[] args) {
    mode = false;

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

    server = null;
    datasocket = null;
    //cPort = udpPort + Integer.parseInt(args[1]);
    initUDP();
    try {


        Scanner sc = new Scanner(new FileReader(commandFile));
        pw = new PrintStream(new File("out_"+args[1]+".txt"));
        System.setOut(pw);

        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");


            if(mode)
            {
              sendTCP(cmd,tokens);
            }
            else
            {
              sendUDP(cmd,tokens);
            }

          /*
          else if (tokens[0].equals("borrow")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
          } else if (tokens[0].equals("return")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
          } else if (tokens[0].equals("inventory")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
          } else if (tokens[0].equals("list")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
          } else if (tokens[0].equals("exit")) {
            // TODO: send appropriate command to the server
          } else {
            System.out.println("ERROR: No such command");
          }*/
        }
    } catch (Exception e) {
	e.printStackTrace();
    }
  }
}
