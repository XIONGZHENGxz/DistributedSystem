
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by xz on 6/7/17.
 */
public class Listener extends Thread{
    private int port;
    private ServerSocket serverSocket;
    public Listener(int port) {
        this.port = port;
    }

    public int getPort() {
        return this.port;
    }

    public ServerSocket getServerSocket() {
        return this.serverSocket;
    }

    @Override
    public void run() {
        try{
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true) {
            try {
                Socket socket = serverSocket.accept();
                String request = Messager.getRequest(socket);
				System.out.println("received request..."+request);
            } catch (IOException e) {
                break;
            }
        }
    }

	public static void main(String...args) {
		Listener l = new Listener(8888);
		l.start();
	}
}
