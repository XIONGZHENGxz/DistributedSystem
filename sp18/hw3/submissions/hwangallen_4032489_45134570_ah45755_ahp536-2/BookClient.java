import java.net.*;
import java.util.Scanner;
import java.io.*;
public class BookClient {
  private Socket server;
  private Scanner din;
  private PrintStream pout;
  private InetAddress iAddress;
  private DatagramSocket dataSocket;
  private DatagramPacket sendPacket, receivePacket;
  byte[] rBuffer = new byte[65507];
  public static void main (String[] args) throws IOException {
    String hostAddress;
    int tcpPort;
    int udpPort;
    int clientId;
    char mode = 'U';
    boolean running = true;

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
    
    // hookup output file
    String fileName = "out_" + clientId + ".txt";
    File file = new File(fileName);
    PrintWriter poutFile = new PrintWriter(new FileWriter(file));

    try {
        Scanner scan = new Scanner(new FileReader(commandFile));
        BookClient client = new BookClient();
        client.getTCPPort(hostAddress, tcpPort);
        client.getUDPPort(hostAddress);

        while(scan.hasNextLine() && running) {
          String cmd = scan.nextLine();
          String[] tokens = cmd.split(" ");

          if (tokens[0].equals("setmode")) {
            // TODO: set the mode of communication for sending commands to the server
            if(tokens[1].equals("T"))
              mode = 'T';
            else if (tokens[1].equals("U"))
              mode = 'U';
              else
            	  System.out.println("Invalid mode set");
          }
          else if (tokens[0].equals("borrow")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
            String studentName = tokens[1];
            String bookName = "";
            for(int i = 2; i < tokens.length - 1; i++)
              bookName += tokens[i] + " ";
            bookName += tokens[tokens.length - 1]; //Format displays it as "Book Name" instead of "Book Name"_; String.trim does not seem to work.
            client.borrow(mode, studentName, bookName, hostAddress, tcpPort, udpPort, poutFile);
          } else if (tokens[0].equals("return")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
            String recordID = tokens[1];
            client.returnBook(mode, recordID, hostAddress, tcpPort, udpPort, poutFile);
          } else if (tokens[0].equals("inventory")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
            client.inventory(mode, hostAddress, tcpPort, udpPort, poutFile);
          } else if (tokens[0].equals("list")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
            String studentName = tokens[1];
            client.listStudent(mode, studentName, hostAddress, tcpPort, udpPort, poutFile);
          } else if (tokens[0].equals("exit")) {
            // TODO: send appropriate command to the server
            client.exit(mode, hostAddress, tcpPort, udpPort);
            poutFile.close();
            running = false;
            scan.close();
          } else {
            System.out.println("ERROR: No such command");
          }
        }
    } catch (Exception e) {
	e.printStackTrace();
    }
  }

  public void getTCPPort(String hostAddress, int port) throws IOException {
    server = new Socket(hostAddress, port);
    din = new Scanner(server.getInputStream());
    pout = new PrintStream(server.getOutputStream());
  }

  public void getUDPPort(String hostAddress) throws UnknownHostException, SocketException {
    iAddress = InetAddress.getByName(hostAddress);
    dataSocket = new DatagramSocket();
  }

  public void borrow(char mode, String studentName, String bookName, String hostAddress, int tcpPort, int udpPort, PrintWriter poutFile) throws IOException {
    if(mode == 'T') {
      //TCP Mode
      pout.println("borrow " + studentName + " " + bookName);
      pout.flush();
      String message = din.nextLine();
      poutFile.println(message);
      poutFile.flush();
    }
    else {
      //UDP Mode
      String command = ("borrow " + studentName + " " + bookName);
      byte[] buffer = command.getBytes();
      sendPacket = new DatagramPacket(buffer, buffer.length, iAddress, udpPort);
      dataSocket.send(sendPacket);
      receivePacket = new DatagramPacket(rBuffer, rBuffer.length);
      dataSocket.receive(receivePacket);
      String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
      poutFile.println(message);
      poutFile.flush();
    }
  }

  public void returnBook(char mode, String recordID, String hostAddress, int tcpPort, int udpPort, PrintWriter poutFile) throws IOException {
    if(mode == 'T') {
      //TCP Mode
      pout.println("return " + recordID);
      pout.flush();
      String message = din.nextLine();
      poutFile.println(message);
      poutFile.flush();
    }
    else {
      //UDP Mode
      String command = "return " + recordID;
      byte[] buffer = command.getBytes();
      sendPacket = new DatagramPacket(buffer, buffer.length, iAddress, udpPort);
      dataSocket.send(sendPacket);
      receivePacket = new DatagramPacket(rBuffer, rBuffer.length);
      dataSocket.receive(receivePacket);
      String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
      poutFile.println(message);
      poutFile.flush();
    }
  }

  public void inventory(char mode, String hostAddress, int tcpPort, int udpPort, PrintWriter poutFile) throws IOException {
    if(mode == 'T') {
      //TCP Mode
      pout.println("inventory");
      pout.flush();
      String message = din.nextLine();
      int lines = Integer.valueOf(message);
      for(int i = 0; i < lines; i++) {
        message = din.nextLine();
        poutFile.println(message);
      }
      poutFile.flush();
    }
    else {
      //UDP Mode
      String command = "inventory";
      byte[] buffer = command.getBytes();
      sendPacket = new DatagramPacket(buffer, buffer.length, iAddress, udpPort);
      dataSocket.send(sendPacket);
      receivePacket = new DatagramPacket(rBuffer, rBuffer.length);
      dataSocket.receive(receivePacket);
      String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
      Scanner tempScanner = new Scanner(message);
      int lines = Integer.parseInt(tempScanner.nextLine());
      for(int i = 0; i < lines; i++) {
        String line = tempScanner.nextLine();
        poutFile.println(line);
      }
      poutFile.flush();
      tempScanner.close();
    }
  }

  public void listStudent(char mode, String studentName, String hostAddress, int tcpPort, int udpPort, PrintWriter poutFile) throws IOException {
    if(mode == 'T') {
      //TCP Mode
      pout.println("list " + studentName);
      pout.flush();
      String message = din.nextLine();
      int lines = Integer.valueOf(message);
      for(int i = 0; i < lines; i++) {
        message = din.nextLine();
        poutFile.println(message);
      }
      poutFile.flush();
    }
    else {
      //UDP Mode
      String command = "list " + studentName;
      byte[] buffer = command.getBytes();
      sendPacket = new DatagramPacket(buffer, buffer.length, iAddress, udpPort);
      dataSocket.send(sendPacket);
      receivePacket = new DatagramPacket(rBuffer, rBuffer.length);
      dataSocket.receive(receivePacket);
      String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
      Scanner tempScanner = new Scanner(message);
      int lines = Integer.parseInt(tempScanner.nextLine());
      for(int i = 0; i < lines; i++) {
        String line = tempScanner.nextLine();
        poutFile.println(line);
      }
      tempScanner.close();
      poutFile.flush();
    }
  }

  public void exit(char mode, String hostAddress, int tcpPort, int udpPort) throws IOException {
    if(mode == 'T') {
      //TCP Mode
      pout.println("exit");
      pout.flush();
    }
    else {
      //UDP Mode
      String command = "exit";
      byte[] buffer = command.getBytes();
      sendPacket = new DatagramPacket(buffer, buffer.length, iAddress, udpPort);
      dataSocket.send(sendPacket);
    }
  }


}