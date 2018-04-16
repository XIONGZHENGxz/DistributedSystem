import java.util.Scanner;
import java.io.*;
import java.util.*;

public class BookClient {

  public static void main (String[] args) {

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

    Transmitter trans;
    Transmitter_TCP newTCPtrans = new Transmitter_TCP(tcpPort);
    Transmitter_UDP newUDPtrans = new Transmitter_UDP(hostAddress, udpPort);
    String outputFile = "out_" + clientID + ".txt";
    File del = new File(outputFile);
    del.delete();
    File newFile = new File(outputFile);
    FileWriter fileWrite;
    BufferedWriter buffWrite;

    try {
      newFile.createNewFile();
      fileWrite = new FileWriter(newFile.getAbsoluteFile(), true);
      buffWrite = new BufferedWriter(fileWrite);
    } catch(IOException e) {
      System.err.println("I/O exception while creating writers");
    }

    try {
        Scanner sc = new Scanner(new FileReader(commandFile));

        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");
          String token = tokens[0];
          if (tokens[0].equals("setmode")) {
            if(tokens[1].equals("U")) {
              trans = newUDPtrans;
            } else {
              trans = newTCPtrans;
            }
            continue;
          }
          else if (tokens[0].equals("borrow")) {
            token += ":" + tokens[1];
            token += ":";
            int index = 2;
            while(1) {
              String tempToken = tokens[index];
              if(tempToken.substring(tempToken.length()-1).equals("\"")) {
                token += tempToken;
                break;
              }
              token += tempToken;
              token += " ";
              index++;
            }
          } else if (tokens[0].equals("return")) {
            token += ":";
            token += tokens[1];
          } else if (tokens[0].equals("inventory")) {
            
          } else if (tokens[0].equals("list")) {
            token += ":";
            token += tokens[1];
          } else if (tokens[0].equals("exit")) {

          } else {
            System.out.println("ERROR: No such command");
            continue;
          }
          String resp = trans.transmit_String(token);
          buffWrite.write(resp);
        }
        newUDPtrans.close();
        newTCPtrans.close();
        fileWrite.close();
        buffWrite.close();
    } catch (FileNotFoundException e) {
	    e.printStackTrace();
    } catch(IOException e) {
      e.printStackTrace();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}
