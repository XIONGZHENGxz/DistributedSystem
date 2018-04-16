import java.util.Scanner;
import java.util.*;
import java.io.File;
import java.net.*;
import java.io.*;


public class BookServer {

  public static void main (String[] args) {
    int tcpPort;
    int udpPort;
    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    String fileName = args[0];
    tcpPort = 7000;
    udpPort = 8000;

    // Inventory
    HashMap<String, Integer> inventory = new HashMap<String, Integer>();
    ArrayList<String> titles = new ArrayList<String>();
    try{
      Scanner s = new Scanner(new File(args[0]));
      while(s.hasNext()){
        String temp = s.nextLine();
        int ind = temp.lastIndexOf(" ");
        inventory.put(temp.substring(0, ind), Integer.parseInt(temp.substring(ind+1)));
        titles.add(temp.substring(0, ind));
      }

    }
    catch(Exception e){
      e.printStackTrace();
    }

    // Create library
    Library lib = new Library(inventory, titles);

    // Start listeners
    UdpListener udbl = new UdpListener(lib, udpPort);
    TcpListener tcpl = new TcpListener(lib, tcpPort);
    udbl.start();
    tcpl.start();

  }

  static class TcpListener extends Thread {

    private ServerSocket dataSocket;
    private Library lib;

    public TcpListener(Library lib, int tcpPort){
      this.lib = lib;
      try {
        this.dataSocket = new ServerSocket(tcpPort);
      }catch(IOException e){
        e.printStackTrace();
      }

    }

    public void run() {
      while(true) {
        try {
          Socket s = dataSocket.accept();
          Thread t = new Thread(new TcpHelper(lib, s));
          t.start();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  static class TcpHelper implements Runnable {

    private Library lib;
    private Socket s;

    public TcpHelper(Library lib, Socket s) {
      this.lib = lib;
      this.s = s;
    }

    public void run() {
      try {
        PrintWriter out = new PrintWriter(s.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

        while(true) {

          String response = null;

          String msg = in.readLine();

          if(!(msg == null)){
            msg = msg.trim();

            String[] tokens = msg.split(" ");

            if (tokens[0].equals("borrow")) {
              response = lib.borrow(tokens[1], msg.substring(msg.indexOf(' ',7)+1));
            } else if (tokens[0].equals("return")) {
              response = lib.to_return(Integer.parseInt(tokens[1]));
            } else if (tokens[0].equals("inventory")) {
              response = lib.inventory_count();
            } else if (tokens[0].equals("list")) {
              response = lib.list(tokens[1]);
            } else if (tokens[0].equals("exit")) {
              response = lib.exit();
          }

          if(response == null){
            continue;
          }

          out.println(response);
          out.flush();

          }


        }

      } catch(IOException e){
        e.printStackTrace();
      }
    }

  }

  static class UdpListener extends Thread {

    private byte[] buf;
    private DatagramSocket dataSocket;
    private Library lib;
    int BUFSIZE;

    public UdpListener(Library lib, int udpPort) {
      BUFSIZE = 2048;
      this.buf = new byte[BUFSIZE];
      try {
        this.dataSocket = new DatagramSocket(udpPort);
      } catch (SocketException e) {
        e.printStackTrace();
      }
      this.lib = lib;
    }

    public void run() {
      while(true) {
        buf = new byte[BUFSIZE];
        DatagramPacket dataPacket = new DatagramPacket(buf, BUFSIZE);
        try {
          dataSocket.receive(dataPacket);
          Thread t = new Thread(new UdpHelper(lib, dataPacket, dataSocket));
          t.start();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  static class UdpHelper implements Runnable {

    private Library lib;
    private DatagramPacket dataPacket;
    private DatagramSocket dataSocket;

    private byte[] responseBytes;
    private String msg = "";
    private String response = "";

    public UdpHelper(Library lib, DatagramPacket dataPacket, DatagramSocket dataSocket) {
      this.lib = lib;
      this.dataPacket = dataPacket;
      this.dataSocket = dataSocket;
    }

    public void run() {

      try {
        msg = new String(dataPacket.getData(), "UTF-8");
      } catch(UnsupportedEncodingException e) {
        e.printStackTrace();
      }

      msg = msg.trim();

      String[] tokens = msg.split(" ");

      if (tokens[0].equals("borrow")) {
        response = lib.borrow(tokens[1], msg.substring(msg.indexOf(' ',7)+1));
      } else if (tokens[0].equals("return")) {
        response = lib.to_return(Integer.parseInt(tokens[1]));

      } else if (tokens[0].equals("inventory")) {
        response = lib.inventory_count();

      } else if (tokens[0].equals("list")) {
        response = lib.list(tokens[1]);

      } else if (tokens[0].equals("exit")) {
        response = lib.exit();
      }

      try {
        responseBytes = response.getBytes("UTF-8");
        DatagramPacket responsePacket = new DatagramPacket(
          responseBytes,
          responseBytes.length,
          dataPacket.getAddress(),
          dataPacket.getPort());
        dataSocket.send(responsePacket);
      } catch(UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      catch(IOException e) {
        e.printStackTrace();
      }

    }
  }

}
