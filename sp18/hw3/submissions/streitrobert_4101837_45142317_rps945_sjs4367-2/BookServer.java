// Robert Streit rps945
// Shyam Sabhaya sjs4367

import java.util.Scanner;
import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.charset.Charset;

public class BookServer {

  private static int tcpPort;
  private static int udpPort;
  private static int counter;
  private static DatagramSocket datasocket;

  public static void main (String[] args) {
    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    String fileName = args[0];
    tcpPort = 7000;
    udpPort = 8000;
    counter = 0;
    Library lib = new Library(fileName);
    //TCP
    Thread tcp = new Thread(new TCPserver());
    tcp.start();

    //UDP
    DatagramPacket datapacket, returnpacket;
    int len = 5000;
    try {

			   datasocket = new DatagramSocket(udpPort);
         //datasocket.setSendBufferSize(100000);
         //datasocket.setReceiveBufferSize(100000);
         //DatagramSocket receiver = new DatagramSocket(udpPort);
			   byte[] buf = new byte[len];
			while (true) {
				datapacket = new DatagramPacket(buf, buf.length);
				datasocket.receive(datapacket);
        counter += 1;
        Thread t = new Thread(new UDPServerThread(datapacket));
        t.start();
				/*returnpacket = new DatagramPacket(
				    datapacket.getData(),
				    datapacket.getLength(),
				    datapacket.getAddress(),
				    datapacket.getPort());
				datasocket.send(returnpacket);*/
			}
		}
    catch(Exception e)
    {
      System.err.println(e);
      e.printStackTrace();
    }

  }
public static void sendUDPpacket(DatagramPacket p)
{
  try{
    datasocket.send(p);
  }
  catch(Exception e)
  {
    e.printStackTrace();
  }
}

public static class Record
{
  public String _book;
  public String _borrower;
  public int _recordNum;

  public Record(String book, String borrower, int recordNum)
  {
    _book = book;
    _borrower = borrower;
    _recordNum = recordNum;
  }
}

public static class Library
{
  private static HashMap<String, Integer> _library = new HashMap<String, Integer>();
  //private static ArrayList<Record> _checkoutRecord = new ArrayList<Record>();
  private static TreeMap<Integer, Record> _checkoutRecord = new TreeMap<Integer, Record>();
  private static ArrayList<String> _bookNames = new ArrayList<String>();
  private static int _currRecordID = 0;

  public Library(String fileName)
  {
    Scanner sc;
    try
    {
      sc = new Scanner(new File(fileName));

      while(sc.hasNextLine())
      {
        String[] line = sc.nextLine().split("\"");
        String book = line[1].trim();
        int num = Integer.parseInt(line[2].trim());
        _library.put(book, num);
        _bookNames.add(book);
      }
      sc.close();
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
  }

  public static synchronized String cmdBorrow(String student, String book)
  {
    book = book.replaceAll("\"", "").trim();
    Integer num = _library.get(book);
    String result;
    if (num != null && num > 0)
    {
      _library.put(book,num-1);
      _currRecordID++;

      result = String.format("Your request has been approved, %d %s \"%s\"", _currRecordID, student, book);
      _checkoutRecord.put(_currRecordID, new Record(book, student, _currRecordID));
    }
    else if (num != null && num == 0)
    {
      result = String.format("Request Failed - Book not available");
    }
    else
    {
      result = String.format("Request Failed - We do not have this book");
    }
    return result;
  }

  public static synchronized String cmdReturn(Integer recordID)
  {
    String result = "";
    boolean notFound = true;

    if(_checkoutRecord.containsKey(recordID))
    {
      String book = _checkoutRecord.get(recordID)._book;
      _checkoutRecord.remove(recordID);
      _library.put(book, _library.get(book) + 1);
      result = String.format("%d is returned", recordID);
    }
    else
    {
      result = String.format("%d not found, no such borrow record", recordID);
    }

    return result;
  }

  public static synchronized String cmdList(String name)
  {
    String result = new String();

    for (Map.Entry<Integer, Record> entry : _checkoutRecord.entrySet())
    {
      if (entry.getValue()._borrower.equals(name))
      {
        if(result.equals(""))
        {
          result += String.format("%d \"%s\"", entry.getValue()._recordNum, entry.getValue()._book);
        }
        else
        {
          result += String.format("@%d \"%s\"", entry.getValue()._recordNum, entry.getValue()._book);
        }
      }
    }
    if(result.equals(""))
    {
      result = String.format("No record found for %s", name);
    }

    return result;
  }

  public static synchronized String cmdInventory()
  {
    String result = new String();

    for (String book : _bookNames)
    {
      if(result.equals(""))
        {
          result += String.format("\"%s\" %d", book, _library.get(book));
        }
        else
        {
          result += String.format("@\"%s\" %d", book, _library.get(book));
        }
    }

    return result;
  }

  public static synchronized void cmdExit()
  {
    try
    {
      PrintWriter writer = new PrintWriter("inventory.txt", "UTF-8");
      writer.println(cmdInventory().replaceAll("@", "\n"));
      writer.flush();
      writer.close();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
}

public static class UDPServerThread extends Thread
{
  DatagramPacket datapacket;
  DatagramSocket datasocket;
  //DatagramSocket datasocket;

  public UDPServerThread(DatagramPacket d)
  {
    datapacket = d;
    try{
      datasocket = new DatagramSocket();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    //datasocket = sock;
  }

  public void sendPort()
  {
    try
    {
      String line = new String(datapacket.getData(),0,datapacket.getLength());
      Scanner st = new Scanner(line);
      st.nextLine();

      String result = datasocket.getLocalPort() + "";
      byte[] bytes = new byte[result.length()];
      bytes = result.getBytes();

      DatagramPacket return_p= new DatagramPacket(bytes, bytes.length,
      datapacket.getAddress(), datapacket.getPort());
      datasocket.send(return_p);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  public void run()
  {

    sendPort();
    int len = 5000;
    while(true)
    {
    try
    {
      byte[] buf = new byte[len];
      datapacket = new DatagramPacket(buf, buf.length);
      datasocket.receive(datapacket);
      String line = new String(datapacket.getData(), 0,datapacket.getLength());
      //System.out.println(line);
      Scanner st = new Scanner(line);
      String result = null;
      String cmd = st.next();

      switch(cmd)
      {
        case "setmode":
          st.nextLine();
          datasocket.close();
          //System.out.println("Got here");
          return;
        case "borrow":
        result = Library.cmdBorrow(st.next(), st.nextLine());
          break;

        case "return":
        result = Library.cmdReturn(st.nextInt());
          break;

        case "inventory":
        result = Library.cmdInventory();
          break;

        case "list":
        result = Library.cmdList(st.next());
          break;

        case "exit":
        Library.cmdExit();
          return;

        default:
        //result = "RESEND";
          return;
      }

      if (result != null)
      {
        byte[] bytes = new byte[result.length()];
        bytes = result.getBytes();

        DatagramPacket return_p= new DatagramPacket(bytes, bytes.length,
        datapacket.getAddress(), datapacket.getPort());
        datasocket.send(return_p);
        //sendUDPpacket(return_p);
      }
    }
    catch(Exception e)
    {
      System.err.println(e);
      e.printStackTrace();
      System.out.println("Probably Here");
    }
  }
  }
}

public static class TCPServerThread extends Thread
{
  Socket client;
  Scanner sc;
  PrintWriter pout;
  public TCPServerThread(Socket s)
  {
    client = s;
    try{
    sc = new Scanner(client.getInputStream());
    pout = new PrintWriter(client.getOutputStream());
  }
  catch(Exception e)
  {
    e.printStackTrace();
  }
  }
  public void run()
  {
    while(true)
    {
    try{
      while(!sc.hasNext());
  		String command = sc.nextLine();
      Scanner st = new Scanner(command);
  		String cmd = st.next();
      switch(cmd)
      {
        case "setmode":
          client.close();
          return;
        case "borrow":
        pout.println(Library.cmdBorrow(st.next(), st.nextLine()));

          break;

        case "return":
        pout.println(Library.cmdReturn(st.nextInt()));
          break;

        case "inventory":
        pout.println(Library.cmdInventory());
          break;

        case "list":
        pout.println(Library.cmdList(st.next()));
          break;

        case "exit":
        Library.cmdExit();
          break;

        default: break;
      }
      pout.flush();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    }
  }
}

public static class TCPserver implements Runnable
{
  public void run()
  {
    try
    {
      ServerSocket listener = new ServerSocket(tcpPort);
      Socket s;
      while ( (s = listener.accept()) != null)
      {
        Thread t = new TCPServerThread(s);
        t.start();
      }
    }
    catch(IOException e)
    {
      System.err.println("Server aborted:" + e);
      e.printStackTrace();
    }

  }
}

}
