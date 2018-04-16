/*
Kyle Bradford (kmb3534)
Spencer Yue (sty223)
*/

import java.io.*;
import java.net.*;
import java.util.*;

public class TCPThread extends Thread
{
  BookServer bs;
  Socket s;
  static boolean verbose = Config.verbose;

  public TCPThread (BookServer bs, Socket s)
  {
    this.bs = bs;
    this.s = s;
  }

  public void run ()
  {
    Scanner in = null;

    try
    {
      in = new Scanner (s.getInputStream ());
      PrintStream out = new PrintStream (s.getOutputStream (), true);

      while (true)
      {
        String command = in.nextLine ();

        if (verbose)
          System.out.println ("Server Received: " + command);

        Scanner cmdScanner = new Scanner (command);
        String tag = cmdScanner.next ();

        if (tag.equals ("setmode"))
        {
          if (cmdScanner.next ().charAt (0) == 'U')
            break;
        }
        // borrow <student-name> <book-name>
        else if (tag.equals ("borrow"))
        {
          out.println (bs.borrowBook (cmdScanner));
        }
        else if (tag.equals ("return"))
        {
          out.println (bs.returnBook (cmdScanner));
        }
        // list <student-name>
        else if (tag.equals ("list"))
        {
          for (String each : bs.listBooks (cmdScanner))
            out.println (each);
        }
        // inventory
        else if (tag.equals ("inventory"))
        {
          for (String each : bs.inventory ())
            out.println (each);
        }
        // exit
        else if (tag.equals ("exit"))
        {
          bs.checkpoint ();
          break;
        }
        else
        {
          cmdScanner.close ();
          throw new Exception ("Bad Input!");
        }
        out.println ("OVER");
        cmdScanner.close ();
      }

      s.close ();
      out.close ();
    }
    catch (Exception e)
    {
      System.err.println (e);
    }
    finally
    {
      if (in != null)
        in.close ();
    }
  }
}
