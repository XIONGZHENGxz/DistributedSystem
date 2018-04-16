import java.net.*; 
import java.io.*; 
import java.util.*;

public class TCPBookServerThread extends Thread {

	private Inventory lib;
	private RecordList rec;
	private Socket s;

	public TCPBookServerThread(Inventory lib, RecordList rec, Socket s) {
		this.lib = lib;
		this.rec = rec;
		this.s = s;
	}

	public void run() {
		//System.out.println("TCP Client Handler spawned");

		try {
			Scanner sc = new Scanner(s.getInputStream());
			PrintWriter pout = new PrintWriter(s.getOutputStream(), true);

			while(sc.hasNextLine()) {
				String command = sc.nextLine();
				//System.out.println("[debug] (" + command + ")"); 

				String result = CommandParser.parseAndExecute(command, lib, rec);
				result = result.trim();

				pout.println(result);
				pout.flush();
				//System.out.println("[debug] (" + result + ")");

				if (result.equals("End thread")) {
					sc.close();
					pout.close();
					s.close();
					return;
				}
			}

		} catch (IOException e) {
			System.err.println(e);
		}
	}
}
