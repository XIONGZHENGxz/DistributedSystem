import java.io.*;

public class DumbRequirements
{
  static void truncateLastLine (String filename) throws Exception
  {
    RandomAccessFile f = new RandomAccessFile (filename, "rw");
    long newLength = f.length () - Config.CRLF.length ();
    if (newLength >= 0)
      f.setLength (newLength);
    f.close ();
  }
}
