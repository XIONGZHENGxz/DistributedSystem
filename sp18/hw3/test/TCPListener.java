/*
Kyle Bradford (kmb3534)
Spencer Yue (sty223)
*/

import java.net.*;

public class TCPListener extends Thread
{
  BookServer bs;
  int port;

  public TCPListener (BookServer bs, int port)
  {
    this.bs = bs;
    this.port = port;
  }

  public void run ()
  {
    try
    {
      ServerSocket listener = new ServerSocket (port);
      Socket s;
      while ((s = listener.accept ()) != null)
      {
        new TCPThread (bs, s).start ();
      }
      listener.close ();
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }
  }
}
