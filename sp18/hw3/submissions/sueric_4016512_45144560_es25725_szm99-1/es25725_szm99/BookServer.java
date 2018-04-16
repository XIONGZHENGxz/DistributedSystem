import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import java.io.*;
import java.net.*;
import java.util.*;

public class BookServer {

  public static class Record {
    String studentName;
    String bookName;

    public Record(String student, String book) {
      studentName = student;
      bookName = book;
    }

    public String getStudent() {
      return studentName;
    }

    public String getBook() {
      return bookName;
    }
  }

  private static HashMap<String, Integer> inventory;
  private static HashMap<Integer, Record> recordBook;
  private static ArrayList<String> order;
  private static volatile int recordId;
  public static ReentrantLock lock;
  public static Condition ready;
  final static int LEN = 1024;

  public static void main(String[] args) {
    int tcpPort;
    int udpPort;
    if (args.length != 1) {
      System.err.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    String fileName = args[0];
    tcpPort = 7000;
    udpPort = 8000;

    // parse the inventory file

    inventory = new HashMap<String, Integer>();
    recordBook = new HashMap<Integer, Record>();
    order = new ArrayList<String>();
    recordId = 1;
    lock = new ReentrantLock(true);
    ready = lock.newCondition();

    try {
      Scanner sc = new Scanner(new FileReader(fileName));

      int i = 0;
      while (sc.hasNextLine()) {
        String item = sc.nextLine().trim();
        int index = item.lastIndexOf(' ');
        String book = item.substring(0, index);
        int count = Integer.parseInt(item.substring(index).trim());
        inventory.put(book, count);
        order.add(book);
      }
      sc.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    try {
      DatagramSocket server = new DatagramSocket(udpPort);
      byte[] rBuff = new byte[LEN];
      // InetAddress ia = InetAddress.getByName("localhost");
      while (true) {
        DatagramPacket rPacket = new DatagramPacket(rBuff, rBuff.length);
        // wait for handshake
        server.receive(rPacket);
        DatagramSocket workerSocket = new DatagramSocket();

        // send port number of workerSocket and spawn worker thread
        byte[] sBuff = new byte[(int) Math.log10(workerSocket.getLocalPort())];
        sBuff = Integer.toString(workerSocket.getLocalPort()).getBytes();
        DatagramPacket sPacket = new DatagramPacket(sBuff, sBuff.length, rPacket.getAddress(), rPacket.getPort());

        Worker worker = new Worker(workerSocket);
        (new Thread(worker)).start();
        server.send(sPacket);
      }
    } catch (SocketException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String execute(String command) {
    String[] tokens = command.split(" ");
    lock.lock();
    String sString = "";
    try {
      switch (tokens[0]) {
      case "setmode": {
        sString = tokens[1];
        break;
      }
      case "borrow": {
        int index = command.indexOf('\"');
        String book = command.substring(index);
        String name = command.substring(command.indexOf(' '), index).trim();
        Integer count = inventory.get(book);
        if (count == null) {
          sString = "Request Failed - We do not have this book";
        } else {
          if (count.intValue() == 0) {
            sString = "Request Failed - Book not available";
          } else {
            // add record
            Record r = new Record(name, book);
            recordBook.put(recordId, r);

            // remove book from inventory
            inventory.put(book, count.intValue() - 1);
            sString = String.format("Your request has been approved, %d %s %s", recordId, name, book);
            recordId++;
          }
        }
        break;
      }
      case "return": {
        int returnID = Integer.parseInt(tokens[1]);
        Record record = recordBook.get(returnID);
        if (record == null) {
          sString = String.format("%d not found, no such borrow record", returnID);
        } else {
          sString = String.format("%d is returned", returnID);
          // update inventory
          int current = inventory.get(record.getBook());
          inventory.put(record.getBook(), current + 1);
          //remove record
          recordBook.remove(returnID);
        }
        break;
      }
      case "list": {
        String name = tokens[1];
        ArrayList<String> borrowedBy = new ArrayList<String>();
        ArrayList<Integer> sorted = new ArrayList<Integer>(recordBook.keySet());
        Collections.sort(sorted);
        for (int i : sorted) {
          Record r = recordBook.get(i);
          if (r.getStudent().equals(name)) {
            borrowedBy.add(String.format("%d %s", i, r.getBook()));
          }
        }
        if (borrowedBy.size() == 0) {
          sString = String.format("No record found for %s", name);
        } else {
          sString = String.join("\n", borrowedBy).trim();
        }
        break;
      }
      case "inventory": {
        ArrayList<String> invList = new ArrayList<String>();
        for (String book : order) {
          invList.add(String.format("%s %d", book, inventory.get(book)));
        }
        sString = String.join("\n", invList).trim();
        break;
      }
      case "exit": {
        sString = tokens[0];
        ArrayList<String> invList = new ArrayList<String>();
        for (String book : order) {
          invList.add(String.format("%s %d", book, inventory.get(book)));
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter("inventory.txt", false));
        bw.write(String.join("\n", invList).trim());
        bw.close();
        break;
      }
      }
    } catch (IOException ie) {
      ie.printStackTrace();
    } finally {
      // ready.notify();
      lock.unlock();
    }

    return sString;
  }

}

class Worker implements Runnable {

  DatagramSocket udpServer;
  String mode;
  ServerSocket tcpServer;

  public Worker(DatagramSocket ds) {
    udpServer = ds;
    mode = "U";
    tcpServer = null;
  }

  /**
   * Handles cases for changing connection type and exiting
   */
  private String setExit(String s) {
    // handle changing connection type 
    String retString = s;
    try {
      if (s.equals("U")) {
        retString = s + " " + udpServer.getLocalPort();
        mode = "U";
      } else if (s.equals("T")) {
        if (tcpServer == null) {
          tcpServer = new ServerSocket(0);
        }
        mode = "T";
        retString = s + " " + tcpServer.getLocalPort();
      } else if (s.equals("exit")) {
        retString = "exit";
      }
    } catch (IOException ie) {
      ie.printStackTrace();
    }

    return retString;

  }

  public void run() {
    try {
      while (true) {
        if (mode.equals("U")) {
          byte[] rBuff = new byte[BookServer.LEN];
          DatagramPacket rPacket = new DatagramPacket(rBuff, rBuff.length);
          udpServer.receive(rPacket);
          String command = new String(rPacket.getData(), 0, rPacket.getLength());
          String sData = BookServer.execute(command);

          sData = setExit(sData);

          byte[] sBuff = new byte[sData.length()];
          sBuff = sData.getBytes();

          DatagramPacket sPacket = new DatagramPacket(sBuff, sBuff.length, rPacket.getAddress(), rPacket.getPort());
          udpServer.send(sPacket);
          if (sData.equals("exit")) {
            if (tcpServer != null) {
              tcpServer.close();
            }
            udpServer.close();
            break;
          }
        } else { // T
          String response = "";
          try (Socket client = tcpServer.accept();
              PrintWriter outputStream = new PrintWriter(client.getOutputStream(), true);
              BufferedReader inputStream = new BufferedReader(new InputStreamReader(client.getInputStream()));) {
            String command = inputStream.readLine();
            response = BookServer.execute(command);

            response = setExit(response);
            outputStream.println(response);

          }

          if (response.equals("exit")) {
            if (tcpServer != null) {
              tcpServer.close();
            }
            udpServer.close();
            break;
          }

        }

      }
    } catch (SocketException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}