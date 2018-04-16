import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by phucd on 3/5/2018.
 */
public class TCPServerThread extends Thread {
	Library library;

	ServerSocket ss;
	Scanner sc;
	PrintStream pout;

	public TCPServerThread(ServerSocket ss, Library l) {
		this.ss = ss;
		this.library = l;
	}

	public void run() {
		try {
			Socket theClient = ss.accept();
			sc = new Scanner(theClient.getInputStream());
			pout = new PrintStream(theClient.getOutputStream());
			while (sc.hasNext()) {
				String command = sc.nextLine();
				System.out.println("received:" + command);
				Scanner st = new Scanner(command);
				String tag = st.next();
				if (tag.equals("borrow")) {
					String student = st.next();
					st.skip(" ");
					String book = st.nextLine();
					int retValue = library.borrow(student, book);
					pout.println(retValue);
					pout.flush();
				} else if (tag.equals("return")) {
					int id = st.nextInt();
					int retValue = library.ret(id);
					pout.println(retValue);
					pout.flush();
				} else if (tag.equals("list")) {
					String student = st.next();
					String retValue = library.list(student);
					pout.println(retValue);
					pout.flush();
				} else if (tag.equals("inventory")) {
					String retValue = library.listInventory();
					pout.println(retValue);
					pout.flush();
				} else if (tag.equals("exit")) {
					String retValue = library.listInventory();
					pout.println(retValue);
					pout.flush();
					ss.close();
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
