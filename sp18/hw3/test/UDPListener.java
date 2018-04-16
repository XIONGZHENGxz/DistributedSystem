/*
Kyle Bradford (kmb3534)
Spencer Yue (sty223)
*/

import java.net.*;

public class UDPListener extends Thread
{
  BookServer bs;
  int port;

  public UDPListener (BookServer bs, int port)
  {
    this.bs = bs;
    this.port = port;
  }

  public void run ()
  {
    byte[] buf = new byte[Config.COMMAND_SIZE];

    DatagramSocket listener = null;
    try
    {
      listener = new DatagramSocket (port);
      while (true)
      {
        DatagramPacket request = new DatagramPacket (buf, buf.length);
        listener.receive (request);

        new UDPThread (bs, request).start ();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }
    finally
    {
      if (listener != null)
        listener.close ();
    }
  }
}
