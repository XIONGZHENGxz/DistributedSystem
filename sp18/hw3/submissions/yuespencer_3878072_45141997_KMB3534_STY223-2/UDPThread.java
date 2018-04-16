/*
Kyle Bradford (kmb3534)
Spencer Yue (sty223)
*/

import java.net.*;
import java.util.*;

public class UDPThread extends Thread
{
  BookServer bs;
  DatagramSocket s;
  DatagramPacket request;
  static boolean verbose = Config.verbose;

  public UDPThread (BookServer bs, DatagramPacket request) throws Exception
  {
    this.bs = bs;
    this.s = new DatagramSocket ();
    this.request = request;
  }

  public void run ()
  {
    String result = Config.CRLF;
    String command = new String (request.getData (), 0, request.getLength ());

    if (verbose)
      System.out.println ("Server Received: " + command);

    Scanner cmdScanner = new Scanner (command);
    String tag = cmdScanner.next ();

    try
    {
      // borrow <student-name> <book-name>
      if (tag.equals("setmode"))
      {}
      else if (tag.equals ("borrow"))
      {
        result = bs.borrowBook (cmdScanner) + Config.CRLF;
      }
      else if (tag.equals ("return"))
      {
        result = bs.returnBook (cmdScanner) + Config.CRLF;
      }
      // list <student-name>
      else if (tag.equals ("list"))
      {
        StringBuilder sb = new StringBuilder ();
        for (String each : bs.listBooks (cmdScanner))
          sb.append (each + Config.CRLF);
        result = sb.toString ();
      }
      // inventory
      else if (tag.equals ("inventory"))
      {
        StringBuilder sb = new StringBuilder ();
        for (String each : bs.inventory ())
          sb.append (each + Config.CRLF);
        result = sb.toString ();
      }
      // exit
      else if (tag.equals ("exit"))
      {
        bs.checkpoint ();
      }
      else
      {
        throw new Exception ("Bad Input!");
      }
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }
    finally
    {
      cmdScanner.close ();
    }

    int numPackets = (result.length () + Config.CONTENT_SIZE - 1) / (Config.CONTENT_SIZE);

    for (int i=0; i<result.length (); i+=Config.CONTENT_SIZE)
    {
      String stamp = String.format ("%04d %04d\n", numPackets, i/Config.CONTENT_SIZE);
      int length = Math.min (result.length () - i, Config.CONTENT_SIZE);
      byte buf[] = (stamp + result.substring (i, i + length)).getBytes ();

      DatagramPacket response = new DatagramPacket (
          buf,
          length + Config.HEADER_SIZE,
          request.getAddress (),
          request.getPort ());

      try
      {
        s.send (response);
      }
      catch (Exception e)
      {
        e.printStackTrace ();
      }
    }

    s.close ();
  }
}
