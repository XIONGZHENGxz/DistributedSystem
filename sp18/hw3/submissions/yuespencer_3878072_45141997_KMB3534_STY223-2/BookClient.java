/*
Kyle Bradford (kmb3534)
Spencer Yue (sty223)
*/

import java.io.*;
import java.net.*;
import java.util.*;

public class BookClient
{
  final static String hostAddress = "localhost";;
  final static int TCP_PORT = 7000; // hardcoded -- must match the server's tcp port
  final static int UDP_PORT = 8000; // hardcoded -- must match the server's udp port
  int CLIENTID;
  char MODE = 'U';
  static boolean verbose = Config.verbose;

  public BookClient (int clientId)
  {
    CLIENTID = clientId;
  }

  public boolean connectTCP (Scanner fileScanner, PrintWriter log) throws Exception
  {
    Socket server = new Socket (hostAddress, TCP_PORT);
    Scanner in = new Scanner (server.getInputStream ());
    PrintStream out = new PrintStream (server.getOutputStream (), true);

    boolean exit = false;
    while (MODE == 'T' && fileScanner.hasNextLine ())
    {
      String command = fileScanner.nextLine ();
      Scanner cmdScanner = new Scanner (command);
      String tag = cmdScanner.next ();

      if (tag.equals ("setmode"))
        MODE = cmdScanner.next ().charAt (0);
      out.println (command);
      exit = tag.equals ("exit");
      if (exit)
        break;

      while (in.hasNextLine ())
      {
        String last = in.nextLine ();
        if (last.equals ("OVER"))
          break;

        log.println (last);

        if (verbose)
          System.out.println ("Client Received: " + last);
      }
      cmdScanner.close ();
    }

    server.close ();
    in.close ();
    out.close ();

    return exit;
  }

  public boolean connectUDP (Scanner fileScanner, PrintWriter log) throws Exception
  {
    boolean exit = false;

    DatagramSocket server = null;
    try
    {
      server = new DatagramSocket ();
      while (MODE == 'U' && fileScanner.hasNextLine ())
      {
        String command = fileScanner.nextLine ();
        Scanner cmdScanner = new Scanner (command);
        String tag = cmdScanner.next ();

        if (tag.equals ("setmode"))
          MODE = cmdScanner.next ().charAt (0);

        byte req[] = command.getBytes ();
        DatagramPacket request = new DatagramPacket (
            req,
            req.length,
            InetAddress.getByName (hostAddress),
            UDP_PORT);

        server.send (request);

        exit = tag.equals ("exit");
        if (exit)
          break;

        byte buf[] = new byte[Config.BLOCK_SIZE];
        DatagramPacket response = new DatagramPacket (buf, buf.length);
        server.receive (response);

        int numBlocks;
        StringBuilder message;
        {
          String result = new String (response.getData (), 0, response.getLength ());
          Scanner resultScanner = new Scanner (result);
          numBlocks = resultScanner.nextInt ();

          message = new StringBuilder (numBlocks * Config.CONTENT_SIZE);
          message.setLength (numBlocks * Config.CONTENT_SIZE);

          int index = resultScanner.nextInt ();
          message.replace (
            index * Config.CONTENT_SIZE,
            (index+1) * Config.CONTENT_SIZE,
            result.substring (Config.HEADER_SIZE));

          resultScanner.close ();
        }

        for (int count=0; count<numBlocks-1; count++)
        {
          response = new DatagramPacket (buf, buf.length);
          server.receive (response);

          String result = new String (response.getData (), 0, response.getLength ());
          Scanner resultScanner = new Scanner (result);
          resultScanner.nextInt (); // skip first token

          int index = resultScanner.nextInt ();
          message.replace (
            index * Config.CONTENT_SIZE,
            (index+1) * Config.CONTENT_SIZE,
            result.substring (Config.HEADER_SIZE));
          resultScanner.close ();
        }

        if (message.length () > Config.CRLF.length ())
          log.print (message.toString ());

        if (verbose)
          System.out.println ("Client Received: " + message.toString ());

        cmdScanner.close ();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }
    finally
    {
      if (server != null)
        server.close ();
    }

    return exit;
  }

  public static void main (String[] args) throws Exception
  {

    if (args.length != 2)
    {
      System.out.println ("ERROR: Provide 2 arguments: commandFile, clientId");
      System.out.println ("\t (1) <command-file>: file with commands to the server");
      System.out.println ("\t (2) client id: an integer between 1..9");
      System.exit (-1);
    }

    if (verbose)
      for (String each : args)
        System.out.println (each);

    Scanner fileScanner = new Scanner (new FileReader (args[0]));
    BookClient bc = new BookClient (Integer.parseInt (args[1]));
    String filename = "out_" + bc.CLIENTID + ".txt";
    PrintWriter log = new PrintWriter (filename);

    while (true)
    {
      if (bc.MODE == 'T')
      {
        if (bc.connectTCP (fileScanner, log))
          break;
      }
      else
      {
        if (bc.connectUDP (fileScanner, log))
          break;
      }
    }

    fileScanner.close ();

    log.flush ();
    log.close ();

    DumbRequirements.truncateLastLine (filename);
  }
}
