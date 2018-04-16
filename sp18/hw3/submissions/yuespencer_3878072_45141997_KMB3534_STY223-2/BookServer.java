/*
Kyle Bradford (kmb3534)
Spencer Yue (sty223)
*/

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import java.util.concurrent.atomic.*;

public class BookServer
{
  ConcurrentHashMap <String, AtomicInteger> library = new ConcurrentHashMap <> ();
  ArrayList <String> bookList = new ArrayList <> ();
  ConcurrentSkipListMap <Integer, Pair <String, String>> history = new ConcurrentSkipListMap <> ();
  AtomicInteger recordId = new AtomicInteger (1);
  static final int TCP_PORT = 7000;
  static final int UDP_PORT = 8000;
  static boolean verbose = Config.verbose;

  public String borrowBook (Scanner cmdScanner) throws Exception
  {
    String result;
    String student = cmdScanner.next ();
    String book = cmdScanner.findInLine (Pattern.compile ("\"[^\"]+\""));
    AtomicInteger quantity = library.get (book);

    if (quantity == null)
    {
      if (verbose)
        System.out.println (book);
      result = "Request Failed - We do not have this book";
    }
    else
    {
      int value = quantity.decrementAndGet ();
      if (value < 0)
      {
        quantity.incrementAndGet ();
        result = "Request Failed - Book not available";
      }
      else
      {
        int id = recordId.getAndIncrement ();
        history.put (id, new Pair <String, String> (student, book));
        result = String.format ("Your request has been approved, %d %s %s", id, student, book);
      }
    }

    return result;
  }

  public String returnBook (Scanner cmdScanner) throws Exception
  {
    String result;
    int id = cmdScanner.nextInt ();
    if (!history.containsKey (id))
    {
      result = id + " not found, no such borrow record";
    }
    else
    {
      String book = history.remove (id).y;

      AtomicInteger quantity = library.get (book);
      quantity.incrementAndGet ();

      result = id + " is returned";
    }

    return result;
  }

  public ArrayList <String> listBooks (Scanner cmdScanner) throws Exception
  {
    ArrayList <String> result = new ArrayList <> ();
    String student = cmdScanner.next ();

    boolean found = false;
    for (Integer id : history.keySet ())
    {
      String s = history.get (id).x;
      String book = history.get (id).y;
      if (s.equals (student))
      {
        result.add (id + " " + book);
        found = true;
      }
    }

    if (!found)
      result.add ("No record found for " + student);

    return result;
  }

  public ArrayList <String> inventory () throws Exception
  {
    ArrayList <String> result = new ArrayList <> ();
    for (String book : bookList)
    {
      int value = Math.max (library.get (book).get (), 0);
      result.add (book + " " + value);
    }

    return result;
  }

  public synchronized void checkpoint () throws Exception
  {
    PrintWriter out = new PrintWriter ("inventory.txt");
    for (String book : bookList)
      out.println (book + " " + library.get (book));
    out.flush ();
    out.close ();

    DumbRequirements.truncateLastLine ("inventory.txt");
  }

  public void initLibrary (String path)
  {
    Scanner scanner = null;
    try
    {
      scanner = new Scanner (new FileReader (path));

      while (scanner.hasNextLine ())
      {
        String command = scanner.nextLine ();
        Scanner cmdScanner = new Scanner (command);
        String book = cmdScanner.findInLine (Pattern.compile ("\"[^\"]+\""));
        int quantity = cmdScanner.nextInt ();
        library.put (book, new AtomicInteger (quantity));
        bookList.add (book);
        cmdScanner.close ();
      }
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace ();
    }
    finally
    {
      if (scanner != null)
        scanner.close ();
    }
  }

  public static void main (String[] args)
  {
    if (args.length != 1)
    {
      for (String each : args)
        System.out.println (each);
      System.out.println ("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit (-1);
    }

    BookServer bs = new BookServer ();

    // parse the inventory file
    bs.initLibrary (args[0]);

    //handle connections
    new TCPListener (bs, TCP_PORT).start ();
    new UDPListener (bs, UDP_PORT).start ();
  }
}
