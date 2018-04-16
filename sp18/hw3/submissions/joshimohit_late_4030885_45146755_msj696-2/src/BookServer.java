import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class BookServer {
	
	static final Semaphore lock = new Semaphore(1, true);
	static final Semaphore idlock = new Semaphore(1, true);
	static Map<String,Integer> inventory = new HashMap<String,Integer>();
	static ArrayList<String> books = new ArrayList<String>();
	static Map<String, ArrayList<Transaction>> transactions = new HashMap<String, ArrayList<Transaction>>();
	static int recordID = 1;
	
    public static void main (String[] args)
    {
	    int tcpPort;
	    int udpPort;
	    if (args.length != 1) {
	      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
	      System.exit(-1);
	    }
	    String fileName = args[0];
	    tcpPort = 7000;
	    udpPort = 8000;
	    FileReader input = null;
		try {
			input = new FileReader(fileName);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	    BufferedReader bufRead = new BufferedReader(input);
	    String myLine = null;
		try {
			myLine = bufRead.readLine();
			while (myLine != null)
		    {    
		        String[] tokens = myLine.split(" ");
		        books.add(myLine.substring(0, myLine.indexOf("\"", 1)+1));
		        inventory.put(myLine.substring(0, myLine.indexOf("\"", 1)+1), Integer.parseInt(tokens[tokens.length-1]));
		        //System.out.println(myLine.substring(0, myLine.indexOf("\"", 1)+1) + " " + Integer.parseInt(tokens[tokens.length-1]));
		        myLine = bufRead.readLine();
		    }
			bufRead.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		Thread udpthread = new Thread(new UDPServer());
		Thread tcpthread = new Thread(new TCPServer());
		udpthread.start();
		tcpthread.start();
    }
    
    private static String borrow(String[] cmd)
    {
    	String name = "";
    	for(int i=2;i<cmd.length;i++)
    	{
    		name += (cmd[i] + " ");
    	}
    	name = name.substring(0, name.length()-1);
    	
    	if(!books.contains(name))
    	{
    		return "Request Failed - We do not have this book\n";
    	}
    	int count = inventory.get(name);
    	if(count == 0)
    	{
    		return "Request Failed - Book not available\n";
    	}
    	
    	Transaction temp = new Transaction(recordID, name);
    	try {
			lock.acquire();
			inventory.replace(name, count-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.release();
		}
    	try {
			idlock.acquire();
			recordID++;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			idlock.release();
		}
    	if(!transactions.containsKey(cmd[1]))
    	{
    		transactions.put(cmd[1], new ArrayList<Transaction>());
    	}
    	transactions.get(cmd[1]).add(temp);
    	
    	return "Your request has been approved, " + (recordID-1) + " " + cmd[1] + " " + name + "\n";
    }
    
    private static String returnToInventory(String[] cmd)
    {
    	for(ArrayList<Transaction> student : transactions.values())
    	{
    		for(Transaction t : student)
    		{
    			if(t.recordID == Integer.parseInt(cmd[1]))
    			{
    				int count = inventory.get(t.bookName);
    				try {
    					lock.acquire();
    					inventory.replace(t.bookName, count+1);
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    				} finally {
    					lock.release();
    				}
    				student.remove(t);
    				return (cmd[1] + " is returned\n");
    			}
    		}
    	}
    	
    	return (cmd[1] + " not found, no such borrow record\n");
    }
    
    private static String inventory()
    {
    	String response = "";
    	for(String book : books)
    	{
    		response += (book + " " + inventory.get(book) + "\n");
    	}
    	
    	return response;
    }
    
    private static String list(String[] cmd)
    {
    	if(!transactions.containsKey(cmd[1]) || transactions.get(cmd[1]) == null)
    	{
    		return ("No record found for " + cmd[1] + "\n");
    	}
    	
    	String response = "";
    	for(Transaction t : transactions.get(cmd[1]))
    	{
    		response += (t.recordID + " " + t.bookName + "\n");
    	}
    	
    	return response;
    }
    
    private static void exit()
    {
    	String filename = "inventory.txt";
        File file = new File(filename);
        try {
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			for(String book : books)
	    	{
	    		output.write(book + " " + inventory.get(book) + "\n");
	    	}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private static class Transaction
    {
    	public int recordID;
    	public String bookName;
    	
    	public Transaction(int rec, String book)
    	{
    		recordID = rec;
    		bookName = book;
    	}
    }
    
    private static class UDPServer implements Runnable
    {
        int udpportnum = 8000;

        @Override
        public void run()
        {
            try {
                DatagramSocket dataSocket = new DatagramSocket(udpportnum);
                byte[] buff = new byte[1024];
                DatagramPacket recievePacket = new DatagramPacket(buff, buff.length);

                while(true)
                {
                    dataSocket.receive(recievePacket);
                    String cmd = new String(recievePacket.getData(), 0, recievePacket.getLength());
                    String[] tokens = cmd.split(" ");

                    DatagramPacket returnPacket = null;
                    String response = "";

                    if (tokens[0].equals("borrow"))
                    {
                        response = borrow(tokens);
                        returnPacket = new DatagramPacket(response.getBytes(), response.length(), recievePacket.getAddress(), recievePacket.getPort());
                        dataSocket.send(returnPacket);
                    }
                    else if (tokens[0].equals("return"))
                    {
                        response = returnToInventory(tokens);
                        returnPacket = new DatagramPacket(response.getBytes(), response.length(), recievePacket.getAddress(), recievePacket.getPort());
                        dataSocket.send(returnPacket);
                    }
                    else if (tokens[0].equals("inventory"))
                    {
                        response = inventory();
                        returnPacket = new DatagramPacket(response.getBytes(), response.length(), recievePacket.getAddress(), recievePacket.getPort());
                        dataSocket.send(returnPacket);
                    }
                    else if (tokens[0].equals("list"))
                    {
                        response = list(tokens);
                        returnPacket = new DatagramPacket(response.getBytes(), response.length(), recievePacket.getAddress(), recievePacket.getPort());
                        dataSocket.send(returnPacket);
                    }
                    else if (tokens[0].equals("exit"))
                    {
                    	dataSocket.close();
                    	exit();
                    }
                    else
                    {
                        System.out.println("ERROR: No such command");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static class TCPServer implements Runnable
    {
        int tcpportnum = 7000;

        @Override
        public void run()
        {
            try
            {
                ServerSocket serverSocket = new ServerSocket(tcpportnum);

                while (true)
                {
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter outputStream = new PrintWriter(clientSocket.getOutputStream());
                    String cmd = input.readLine();
                    while(cmd != null)
                    {
                        String response = "";
                        String[] tokens = cmd.split(" ");
                        
                        if (tokens[0].equals("borrow"))
                        {
                            response = borrow(tokens);
                            outputStream.print(response);
                            outputStream.flush();
                        }
                        else if (tokens[0].equals("return"))
                        {
                            response = returnToInventory(tokens);
                            outputStream.print(response);
                            outputStream.flush();
                        }
                        else if (tokens[0].equals("inventory"))
                        {
                            response = inventory();
                            outputStream.print(response);
                            outputStream.flush();
                        }
                        else if (tokens[0].equals("list"))
                        {
                            response = list(tokens);
                            outputStream.print(response);
                            outputStream.flush();
                        }
                        else if (tokens[0].equals("exit"))
                        {
                        	serverSocket.close();
                            exit();
                        }
                        else
                        {
                            //System.out.println("ERROR: No such command");
                        }
                        cmd = input.readLine();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
