import java.net.*; import java.io.*; import java.util.*;
public class ServerThread extends Thread {
	NameTable table;
	Socket theClient;
	boolean udpMode;
	public ServerThread(NameTable table, Socket s, boolean udpMode) {
		this.table = table;
		this.theClient = s;
		this.udpMode = udpMode;
		
	}
	@Override public void run() {
		try {
				InputStreamReader in = new InputStreamReader(theClient.getInputStream());
				BufferedReader bufin = new BufferedReader(in);
				PrintWriter pout = new PrintWriter(theClient.getOutputStream(), true);
				String command = "";
				while( (command = bufin.readLine() ) != null) {
				String retString = "";
				String tokens[] = command.split("\\s+", 2);
				if (tokens[0].equals("setmode")) {
					if(tokens[1].equals("T"))
						table.setMode(false);
					else
						table.setMode(true);
					
				} else if (tokens[0].equals("borrow")) {
					String[] dupSplit = tokens[1].split("\\s+", 2);
					dupSplit[1] = dupSplit[1].replace("\"", "");
					int retVal = table.borrow(dupSplit[0], dupSplit[1]);
					//System.out.println(retVal);
					if(retVal == 0)
						retString = "Request Failed - Book not available";
					else if (retVal == -1)
						retString= "Request Failed - We do not have this book";
					else
						retString = "Your request has been approved, " +retVal + " " + dupSplit[0] + " \"" + dupSplit[1] + "\"";
						
				} else if (tokens[0].equals("return")) {                  
					int val = Integer.parseInt(tokens[1]);
					int retVal = table.returnBook(val);
					if(retVal == -1)
						retString = val + " not found, no such borrow record";
					else
						retString=val + " is returned";
					
				} else if(tokens[0].equals("inventory")){
					retString = table.printInventory();
				} else if (tokens[0].equals("list")){
					 retString = table.printList(tokens[1]);
					if(retString.equals("")){
						retString="No record found for " + tokens[1] ;
					}
				} else if (tokens[0].equals("exit")){
					retString = table.printInventory();
				}
				//System.out.println(retString);
				pout.println(retString);
				pout.flush();
				}
				bufin.close();
				pout.close();
				theClient.close();
				
			} catch (IOException e) {
				System.err.println(e);
			}
		}
}
