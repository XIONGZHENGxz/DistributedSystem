import java.io.*;
import java.util.*;

public class BookServer {

  private static ArrayList<String> print = new ArrayList<String>();
  private static ArrayList<LibraryRecord> recordList = new ArrayList<Record>();
  private static HashMap<String, Integer> lib_inventory = new HashMap<String, Integer>();
  private static int recordnum = 1;

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

    // parse the inventory file
    try {
      FileReader inFile = new FileReader(args[0]);
      BufferedReader buffer = new BufferedReader(inFile);
      String temp = null;
      while((temp = buffer.readLine()) != null) {
        String[] strArray = temp.split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        String book = strArray[0];
        Integer numBook = Integer.valueOf(lineArray[1]);
        print.add(book);
        lib_inventory.put(book, numBook);
      }
    } catch(Exception e) {
      System.err.println("Exception while parsing inventory.");
    }
    // TODO: handle request from clients
    Server_TCP newTCPServer = new Server_TCP();
    Server_UDP newUDPserver = new Server_UDP();
    newTCPServer.start();
    newUDPServer.start();
  }

  public synchronized static int borrow(String student, String book, ) {
    book = book.trim();
    if(lib_inventory.get(book) == 0 || lib_inventory.get(book).intValue()-1 < 0) {
      return -1;
    }
    Integer tempint = new Integer(lib_inventory.get(book).intValue()-1);
    lib_inventory.put(book, tempint);
    recordList.add(new LibraryRecord(student, book, recordnum));
    return recordnum++;
  }

  public synchronized static int return(int recID) {
    LibraryRecord exists = null;
    for(LibraryRecord rec : recordList) {
      if(rec.record_ID == recID) {
        exists = rec;
        break;
      }
    }
    if(exists != null) {
      recordList.remove(exists);
      Integer next = new Integer(lib_inventory.get(exists.bookName).intValue()+1);
      lib_inventory.put(exists.bookName, next);
      return 1;
    }
    return 0;
  }

  public synchronized static int listLength(String stuname) {
    ArrayList<LibraryRecord> studentRecs = new ArrayList<LibraryRecord>();
    for(LibraryRecord rec : recordList) {
      if(rec.name.equals(stuname))
        studentRecs.add(rec);
    }
    return studentRecs.size();
  }

  public synchronized static String list(String stuname) {
    String listOut;
    ArrayList<LibraryRecord> studentRecs = new ArrayList<LibraryRecord>();
    for(LibraryRecord rec : recordList) {
      if(rec.name.equals(stuname)) {
        studentRecs.add(rec);
      }
    }
    if(studentRecs.size() == 0) {
      listOut = "No record found for " + stuname + "\n";
    } else {
      for(LibraryRecord rec : studentRecs) {
        listOut += rec.record_ID + " " + rec.bookName + "\n";
      }
    }
    return listOut;
  }

  public static int inventoryLength() {
    return lib_inventory.keySet().size();
  }

  public synchronized static String inventory() {
    String invOut;
    for(String x : print) {
      invOut += x + " " + lib_inventory.get(x) + "\n";
    }
    return invOut;
  }

  public synchronized static void inventoryOutput() {
    File del = new File("inventory.txt");
    del.delete();
    File newFile = new File("inventory.txt");
    FileWriter fileWrite;
    BufferedWriter buffWrite;
    try {
      newFile.createNewFile();
      fileWrite = new FileWriter(newFile.getAbsoluteFile(), true);
      buffWrite = new BufferedWriter(fileWrite);
      String output;
      for(String x : print) {
        output += x + " " + lib_inventory.get(x) + "\n";
      }
      buffWrite.write(output);
      buffWrite.close();
      fileWrite.close();
    } catch(Exception e) {
      System.err.println("Exception in inventory output");
    }
  }

}
