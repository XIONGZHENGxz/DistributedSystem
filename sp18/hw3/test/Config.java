/*
Kyle Bradford (kmb3534)
Spencer Yue (sty223)
*/

public class Config
{
  static final boolean verbose = false;
  static final String CRLF = System.lineSeparator();
  public static final int BLOCK_SIZE = 1024;
  public static final int HEADER_SIZE = 1 + 4 + 1 + 4;
  public static final int CONTENT_SIZE = BLOCK_SIZE - HEADER_SIZE;
  public static final int COMMAND_SIZE = 1024;
}
