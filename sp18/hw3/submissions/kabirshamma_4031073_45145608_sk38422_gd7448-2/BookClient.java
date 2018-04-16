import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookClient {
    static File file;
    static BufferedWriter bw;

    public synchronized static String parseTitle(String line) {
        Pattern P1 = Pattern.compile("\"([^\"]*)\"");
        Matcher m1 = P1.matcher(line);
        String title = null;
        while (m1.find()) {
            //System.out.println(m1.group(0));
            title = m1.group(1);
        }
        return title;
    }

    private synchronized static String clientUDP(String hostAddress, int udpPort, String command) {
        DatagramSocket clientSocket;
        InetAddress ia;
        DatagramPacket sPacket, rPacket;
        byte[] rbuffer = new byte[1024];

        try {
            clientSocket = new DatagramSocket();
            ia = InetAddress.getByName(hostAddress);
            byte[] buffer = new byte[command.length()];
            buffer = command.getBytes();
            sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
            clientSocket.send(sPacket);
            rPacket = new DatagramPacket(rbuffer, rbuffer.length);

            String[] tokens = command.split(" ");
            clientSocket.receive(rPacket);
            String retVal = new String(rPacket.getData(), 0, rPacket.getLength());

            if(!(tokens[0].equals("exit"))) {
                bw.write(retVal);
                bw.newLine();
            }
            return retVal;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new String();
    }

    private synchronized static String clientTCP(String hostAddress, int tcpPort, String command) {
        Scanner fromServer;
        Socket clientSocket;
        DataOutputStream outToServer;
        String retVal;
        PrintStream pout;

        try {
            clientSocket = new Socket(hostAddress, tcpPort);
            pout = new PrintStream(clientSocket.getOutputStream());
            fromServer = new Scanner(clientSocket.getInputStream());

            String[] tokens = command.split(" ");
            //System.out.println("TESTING COMMAND IS: " + tokens[0]);
            pout.println(command);
            pout.flush();
            StringBuilder temp = new StringBuilder();
            String a = fromServer.nextLine();
            if (tokens[0].equals("inventory") || tokens[0].equals("list")) {
                while (!a.equals("DONE")) {
                    temp.append(a + "\n");
                    a = fromServer.nextLine();
                }
                //retVal = fromServer.nextLine();
                retVal = temp.toString();
            }
            else {
                retVal = a + "\n";
            }
            if(!(tokens[0].equals("exit"))) {
                bw.write(retVal);
                //bw.newLine();
            }
            clientSocket.close();
            return retVal;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new String();

    }

    private static synchronized String clientHelper(String hostAddress, int tcpPort, int udpPort, String command, String protocol) {
        if (protocol.equals("UDP")) {
            return clientUDP(hostAddress, udpPort, command);
        } else if (protocol.equals("TCP")) {
            return clientTCP(hostAddress, tcpPort, command);
        }
        return "fuk";
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String hostAddress;
        int tcpPort;
        int udpPort;
        int clientId;
        String filename = "out_client" + args[1] + ".txt";

        if (args.length != 2) {
            System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
            System.out.println("\t(1) <command-file>: file with commands to the server");
            System.out.println("\t(2) client id: an integer between 1..9");
            System.exit(-1);
        }
        try {
            file = new File(filename);
            PrintWriter pw = new PrintWriter(filename);
            pw.close();
            bw = new BufferedWriter(new FileWriter(file, true));
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }


        String commandFile = args[0];
        clientId = Integer.parseInt(args[1]);
        hostAddress = "localhost";
        tcpPort = 7000;// hardcoded -- must match the server's tcp port
        udpPort = 8000;// hardcoded -- must match the server's udp port

        Scanner sc = null;
        Scanner fromServer = null;
        Socket clientSocket = null;
        DataOutputStream outToServer = null;
        //DataInputStream fromServer = null;
        PrintStream pout = null;
        String currProtocol = null;

        //try {
        sc = new Scanner(new FileReader(commandFile));

        while (sc.hasNextLine()) {
            //Thread.sleep(1500);
            String cmd = sc.nextLine();
            String[] tokens = cmd.split(" ");
            System.out.println(cmd);
            Thread.sleep(1000);

            if (tokens[0].equals("setmode")) {
                if (tokens[1].equals("U")) {
                    currProtocol = "UDP";
                } else {
                    currProtocol = "TCP";
                }

            } else if (tokens[0].equals("borrow")) {

                String retValue = clientHelper(hostAddress, tcpPort, udpPort, cmd, currProtocol);
                System.out.println(retValue);


            } else if (tokens[0].equals("return")) {
                //System.out.println("Returning book....");
                String retValue = clientHelper(hostAddress, tcpPort, udpPort, cmd, currProtocol);
                System.out.println(retValue);

            } else if (tokens[0].equals("inventory")) {

                String retValue = clientHelper(hostAddress, tcpPort, udpPort, cmd, currProtocol);
                String[] retTokens = retValue.split("\n");
                for (String a : retTokens) {
                    System.out.println(a);
                }


            } else if (tokens[0].equals("list")) {

                String retValue = clientHelper(hostAddress, tcpPort, udpPort, cmd, currProtocol);
                String[] retTokens = retValue.split("\n");
                for (String a : retTokens) {
                    System.out.println(a);
                }

            } else if (tokens[0].equals("exit")) {

                String retValue = clientHelper(hostAddress, tcpPort, udpPort, cmd, currProtocol);
                //System.out.println("Client exit: " + retValue);
                BufferedWriter buff = new BufferedWriter(new FileWriter(new File("inventory.txt"), false));
                Scanner tempSc = new Scanner(retValue);
                buff.write(retValue);
                buff.newLine();
                buff.close();
                bw.close();
                break;//stop processing commands that come post exit?


            } else {
                System.out.println("ERROR: No such command");
            }

        }
    }
}


/*import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookClient {

  public static String parseTitle(String line){
    Pattern P1 = Pattern.compile("\"([^\"]*)\"");
    Matcher m1 = P1.matcher(line);
    String title = null;
    while (m1.find()) {
      //System.out.println(m1.group(0));
      title = m1.group(1);
    }
    return title;
  }

  private static String clientUDP(String hostAddress, int udpPort, String command) {
    DatagramSocket clientSocket;
    InetAddress ia;
    DatagramPacket sPacket, rPacket;
    byte[] rbuffer = new byte[1024];

    try {
      clientSocket = new DatagramSocket();
      ia = InetAddress.getByName(hostAddress);
      byte[] buffer = new byte[command.length()];
      buffer = command.getBytes();
      sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
      clientSocket.send(sPacket);
      rPacket = new DatagramPacket(rbuffer, rbuffer.length);

      clientSocket.receive(rPacket);
      String retVal = new String(rPacket.getData(), 0, rPacket.getLength());

      return retVal;
    }
   catch (UnknownHostException e) {
    e.printStackTrace();
    } catch (IOException e) {
    e.printStackTrace();
    }

    return new String();
  }

  private static String clientTCP(String hostAddress, int tcpPort, String command) {
    Scanner fromServer;
    Socket clientSocket;
    DataOutputStream outToServer;
    String retVal;
    PrintStream pout;

    try {
      clientSocket = new Socket(hostAddress,tcpPort);
      pout = new PrintStream(clientSocket.getOutputStream());
      fromServer = new Scanner(clientSocket.getInputStream());

      pout.println(command);
      pout.flush();
      retVal = fromServer.nextLine();
      clientSocket.close();
      return retVal;
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return new String();

  }
  private static String clientHelper(String hostAddress, int tcpPort, int udpPort, String command, String protocol) {
    if(protocol.equals("UDP")) {
      return clientUDP (hostAddress, udpPort, command);
    } else if(protocol.equals("TCP")) {
      return clientTCP (hostAddress, tcpPort, command);
    }
    return "fuk";
  }
  public static void main (String[] args) throws IOException {
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

    Scanner sc = null;
    Scanner fromServer = null;
    Socket clientSocket = null;
    DataOutputStream outToServer = null;
    //DataInputStream fromServer = null;
    PrintStream pout = null;
    String currProtocol = null;

    //try {
      sc = new Scanner(new FileReader(commandFile));

      while(sc.hasNextLine()) {
        String cmd = sc.nextLine();
        String[] tokens = cmd.split(" ");
        System.out.println(cmd);

        if (tokens[0].equals("setmode")) {
          if (tokens[1].equals("U")) {
            currProtocol = "UDP";
          }
          else {
            currProtocol = "TCP";
          }

        }
        else if (tokens[0].equals("borrow")) {
          //System.out.println("borrowing...");
          //pout.println(cmd);
          //pout.flush();
          //String retValue = fromServer.nextLine();
          String retValue = clientHelper(hostAddress, tcpPort, udpPort, cmd, currProtocol);
          System.out.println(retValue);


        } else if (tokens[0].equals("return")) {
          //System.out.println("Returning book....");
          String retValue = clientHelper(hostAddress, tcpPort, udpPort, cmd, currProtocol);
          System.out.println(retValue);

        } else if (tokens[0].equals("inventory")) {
          //System.out.println("Listing inventory....");
          /*pout.println(cmd);
          pout.flush();
          while (fromServer.hasNext()) {
            String retValue = fromServer.nextLine();
            System.out.println(retValue);

          }*/
/*
          String retValue = clientHelper(hostAddress, tcpPort, udpPort, cmd, currProtocol);
          String[] retTokens = retValue.split(",");
          for (String a: retTokens) {
            System.out.println(a);
          }



        } else if (tokens[0].equals("list")) {
          //System.out.println("Listing records....");
          /*pout.println(cmd);
          pout.flush();
          while (fromServer.hasNext()) {
            String retValue = fromServer.nextLine();
            System.out.println(retValue);

          }*/
/*
          String retValue = clientHelper(hostAddress, tcpPort, udpPort, cmd, currProtocol);
          String[] retTokens = retValue.split(",");
          for (String a: retTokens) {
            System.out.println(a);
          }

          //outToServer.writeBytes(cmd);

          // appropriate responses form the server

        } else if (tokens[0].equals("exit")) {
          //outToServer.writeBytes(cmd);

        } else {
          System.out.println("ERROR: No such command");
        }

      }
    /*} catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }finally {
      outToServer.close();
      clientSocket.close();
    }*/
//  }
//}
