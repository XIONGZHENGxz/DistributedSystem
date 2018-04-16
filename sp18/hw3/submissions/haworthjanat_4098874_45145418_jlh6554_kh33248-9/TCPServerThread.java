import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class TCPServerThread implements Runnable{
	int len = 1024;
	Socket theClient;
	BookServer bs;
	
	TCPServerThread(BookServer bs, Socket client) {
		theClient = client;
		this.bs = bs;
	}

	@Override
	public void run() {
		try {
			Scanner sc = new Scanner(theClient.getInputStream());
			PrintWriter pout = new PrintWriter(theClient.getOutputStream());
			String serverMessage = sc.nextLine();
			
			ArrayList<String> returnStrings = new ArrayList<>();
			parseCommands(serverMessage, returnStrings);
			
			StringBuilder returnString = new StringBuilder();
			
			for (int i = 0; i < returnStrings.size(); i++) {
				returnString.append(returnStrings.get(i));
			}
			pout.print(returnString);
			pout.flush();
			sc.close();
			theClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void parseCommands(String serverMessage, List<String> returnStrings) {
		String[] commands = serverMessage.split(" ");
		String command = commands[0];
		
		if (command.equals("setmode")) {
			
		}
		else if (command.equals("borrow")) {
			String userName = commands[1];
			String[] tempArray = serverMessage.split("\"");
			String bookTitle = tempArray[1];
			bs.syncBorrow(userName, bookTitle, returnStrings);
		}
		else if (command.equals("return")) {
			int recordId = Integer.valueOf(commands[1]);
			bs.syncReturn(recordId, returnStrings);
		}
		else if (command.equals("list")) {
			String userName = commands[1];
			bs.syncList(userName, returnStrings);
		}
		else if (command.equals("inventory")) {
			bs.syncInventory(returnStrings);
		}
		else if (command.equals("exit")) {
			bs.syncExit(returnStrings);
		}
	}
}
