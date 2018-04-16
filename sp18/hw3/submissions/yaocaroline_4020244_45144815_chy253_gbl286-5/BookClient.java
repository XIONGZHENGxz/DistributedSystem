//package hwk3;

import java.util.Scanner;
import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class BookClient {

  private String currConnection;
  private int id;
  Socket tcpSocket;
  DatagramSocket udpSocket;
  int tcpPort;
  int udpPort;
  Scanner in;
  PrintStream out;
  int len;


  public BookClient(String addr, int tcpPort, int udpPort) throws IOException {
    currConnection = "U";
    this.tcpPort = tcpPort;
    this.udpPort = udpPort;
    udpSocket = new DatagramSocket();
    tcpSocket = new Socket(addr,tcpPort);
    in = new Scanner(tcpSocket.getInputStream());
    out = new PrintStream(tcpSocket.getOutputStream());
    len = 2048;
  }

  public void setUDPSocketAndPort(InetAddress ia, DatagramSocket serverUDPSocket, int serverUDPPort) throws IOException {

      // send connection request to UDP listener
      byte[] initBuff = "initial contact".getBytes();
      DatagramPacket initPacket = new DatagramPacket(initBuff, initBuff.length, ia, serverUDPPort);
      serverUDPSocket.send(initPacket);

      // wait to receive unique udp port
      byte[] rbuffer = new byte[len];
      DatagramPacket portPacket = new DatagramPacket(rbuffer, rbuffer.length);
      serverUDPSocket.receive(portPacket);
      this.udpPort = Integer.parseInt(new String(portPacket.getData(), 0, portPacket.getLength()));

  }

  public void setId(int id){
    this.id = id;
  }

  public String getServerResponse(String command, InetAddress ia) throws IOException {
      StringBuilder returnString = new StringBuilder();
      if (currConnection.equals("U")) {
          //send the cmd line
          byte[] stringMess = command.getBytes();
          DatagramPacket sPacket = new DatagramPacket(stringMess, stringMess.length, ia, udpPort);
          udpSocket.send(sPacket);

          //wait for the response
          byte[] rBuffer = new byte[len];
          DatagramPacket rPacket = new DatagramPacket(rBuffer, rBuffer.length);
          udpSocket.receive(rPacket);
          String line = new String(rPacket.getData(), 0, rPacket.getLength());
          while(!line.equals("end")){
              returnString.append(new String(rPacket.getData(), 0, rPacket.getLength()));
              rPacket = new DatagramPacket(rBuffer, rBuffer.length);
              udpSocket.receive(rPacket);
              line = new String(rPacket.getData(), 0, rPacket.getLength());
          }

      } else {
          //send the cmd line
          out.println(command);
          out.flush();

          //wait for the response
          String prefix = "";
          while (in.hasNextLine()) {
              String retString = in.nextLine();
              if(retString.equals("end")) break;
              returnString.append(prefix);
              prefix = "\n";
              returnString.append(retString);
          }
      }
      return returnString.toString();
  }

  public void switchMode(String hostAddress, int serverTCPPort, InetAddress ia, int serverUDPPort, DatagramSocket serverUDPSocket) throws IOException {
      // switch to TCP
      if (currConnection.equals("U")){
          currConnection = "T";
      }
      // switch to UDP
      else {
          currConnection = "U";
      }
  }
  
  public static void main (String[] args) {
      String hostAddress = "localhost";
      int serverTCPPort = 7000;// hardcoded -- must match the server's tcp port
      int serverUDPPort = 8000;// hardcoded -- must match the server's udp port

    if (args.length != 2) {
      System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
      System.out.println("\t(1) <command-file>: file with commands to the server");
      System.out.println("\t(2) client id: an integer between 1..9");
      System.exit(-1);
    }

    try {
        BookClient client = new BookClient(hostAddress, serverTCPPort, serverUDPPort);
        String commandFile = args[0];
        client.setId(Integer.parseInt(args[1]));

        Scanner sc = new Scanner(new FileReader(commandFile));
        InetAddress ia = InetAddress.getByName(hostAddress);

        // connect to server
        DatagramSocket serverUDPSocket = new DatagramSocket();
        // connect to UDP thread port
        client.setUDPSocketAndPort(ia, serverUDPSocket, serverUDPPort);

        // create output file
        PrintWriter writer = new PrintWriter("out_"+client.id+".txt", "UTF-8");

        /*// for testing
        int line = 0;*/
        while(sc.hasNextLine()) {
            String cmd = sc.nextLine();
            String[] tokens = cmd.split(" ");

            /* //for testing
            if(line == 40 && client.id == 1) {
                System.out.println("sleep for a bit");
                TimeUnit.SECONDS.sleep(2);
            }
            if(line == 20 && client.id == 2) TimeUnit.MILLISECONDS.sleep(200);
            */

            if (tokens[0].equals("setmode")){
                // only change if setting to new mode, otherwise do nothing
                if(!tokens[1].equals(client.currConnection)) {
                    client.switchMode(hostAddress, serverTCPPort, ia, serverUDPPort, serverUDPSocket);
                }
            } else {
                String retString = client.getServerResponse(cmd,ia);
                if(retString.equals("client exit")) break;

                // write output to client out file
                if(tokens[0].equals("inventory") || tokens[0].equals("list")){
                    String[] items = retString.split("\\n");
                    for(String s : items){
                        writer.println(s);
                    }
                    writer.flush();
                } else {
                    writer.println(retString);
                    writer.flush();
                }
            }
        }

        writer.close();
        sc.close();
        client.udpSocket.close();
        client.tcpSocket.close();
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
}
