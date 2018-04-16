import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.io.InputStreamReader;

public class BookServer {
  private int recordId = 1;
  
  private LinkedHashMap<String,Integer> checkedIn = new LinkedHashMap<String, Integer>();
  private HashMap<Integer, Student> checkedOut = new HashMap<Integer, Student>();
  private Set<Integer> curClients = new HashSet<Integer>();
  
  public static void main (String[] args) {
    final int tcpPort = 7000;
    final int udpPort = 8000;
    
    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    String fileName = args[0];
    
    BookServer bs = new BookServer();
    bs.parseInventory(fileName);
    bs.runTCP(tcpPort);
    bs.runUDP(udpPort);
  } 
  public void parseInventory(String filename) {
	  try {
			BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
			String st;
			while ((st = br.readLine()) != null) {
			    //System.out.println(st);
				int i;
				for(i = st.length()-1; i >= 0; i--) {
					if(!Character.isDigit(st.charAt(i))) {
						break;
					}
				}
				String bb = st.substring(0, i);
				checkedIn.put(bb, Integer.parseInt(st.substring(i+1)));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  }
  
//  send packets format:
//  clientID command
  public void runTCP(int port) {
	  final int tcpPort = port;
	  
	  Thread tcp_connection = new Thread(new Runnable() { public void run() { 
		  try {
				ServerSocket  listener = new ServerSocket(tcpPort);
				Socket s;
				while((s=listener.accept())!=null){
					Thread t = new Thread(new TCPClient(s));
					t.start();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }});
	  
	  tcp_connection.start();
  }
  public void runUDP(int port) {
	  final int udpPort = port;
	  
	  Thread udp_connection = new Thread(new Runnable() { public void run() { 
	    	DatagramSocket datasocket;
	    	
			try {
				datasocket = new DatagramSocket(udpPort);
		    	DatagramPacket datapacket , returnpacket;
		    	byte[] buf = new byte[1024];
		    	
		    	while(true){
		    		datapacket = new DatagramPacket(buf ,buf.length);
		    		datasocket.receive(datapacket);
		    		String command = new String(datapacket.getData(), 0, datapacket.getLength());
		    		Scanner sc = new Scanner(command);
		    		
					String return_data = executeCommand(sc);
					if (!return_data.equals("-1")) {
						byte[] buffer = new byte[return_data.length()];
						buffer = return_data.getBytes();
			    		returnpacket = new DatagramPacket(buffer,buffer.length ,datapacket.getAddress() ,datapacket.getPort ());
			    		datasocket.send(returnpacket);
					}
		    	}
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }});
	  
	  udp_connection.start();
  }
  
  public synchronized String executeCommand(Scanner sc) {
	  	int clientID = Integer.parseInt(sc.next());
		String command = sc.next();
//		if (command == "setmode") {
//			curClients.add(clientID);
//		}
//		else if (!curClients.contains(clientID)) {
//			return "-1";
//		}
		
		if(command.equals("borrow")) {
			String student = sc.next();
			String book = sc.nextLine();
			book = book.substring(1, book.length());
			return borrowBook(student, book);
		}
		else if (command.equals("return")) {
			int recordId = sc.nextInt();
			return returnBook(recordId);
		}
		else if (command.equals("list")) {
			String student = sc.next();
            student = student.substring(0, student.length());
			return listBooks(student);
		}
		else if (command.equals("inventory")) {
			return inventory();
		}
		else if (command.equals("exit")) {
			exit(clientID);
			return "exit";
		}
		else {
			return "-1";
		}
  }
  
  public synchronized String borrowBook(String student, String book) {
	  StringWriter strOut = new StringWriter();
	  if (checkedIn.containsKey(book) && (checkedIn.get(book) == 0)) {
		  strOut.write("Request Failed - Book not available");
	  }
	  else if (!checkedIn.containsKey(book)) {
		  strOut.write("Request Failed - We do not have this book");
	  }
	  else {
		  int id = getRecordId();
		  strOut.write("You request has been approved, "+id +" " + student + " " + book);
		  int cnt = checkedIn.get(book);
		  cnt--;
		  checkedIn.put(book, cnt);
		  Student s  = new Student(student, book, id);
		  checkedOut.put(id, s);
	  }
	  String output = strOut.toString();
	  return output;
  }
  
  public synchronized String returnBook(int id) {
	  StringWriter strOut = new StringWriter();
	  if (!checkedOut.containsKey(id)) {
		  strOut.write(id+" not found, no such borrow record");
	  }
	  else {
		  String book = checkedOut.get(id).book;
		  checkedOut.remove(id);
		  int val = checkedIn.get(book);
		  val++;
		  checkedIn.put(book, val);
		  
		  strOut.write(id+" is returned");
	  }
	  String output = strOut.toString();
	  return output;
  }
  
  public synchronized String listBooks(String student) {
	  StringWriter strOut = new StringWriter();
	  ArrayList<String> arr = new ArrayList<String>();
	  boolean exist = false;
	  for (Student s: checkedOut.values()) {
		  if (s.name.equals(student)) {
			  exist = true;
			  strOut.write(s.id+" "+s.book+"%%%");
		  }
	  }
	  if (!exist) {
		  strOut.write("No record found for "+student);
	  }
	  String output = strOut.toString();
	  return output;
  }
  
  public synchronized String inventory() {
	  StringWriter strOut = new StringWriter();
	  
	  for(Entry<String, Integer> s: checkedIn.entrySet()) {
		  strOut.write(s.getKey() + " " + s.getValue()+"%%%");
	  }
	  
	  String output = strOut.toString();
	  return output;
  }
  
  public synchronized void exit(int client) {
	  PrintWriter writer;
	  try {
		writer = new PrintWriter("inventory.txt", "UTF-8");
		String invent = inventory();
        String[] responseArr = invent.split("%%%");
        for (String str: responseArr) {
          writer.print(str);
          writer.println();
        }
	    writer.close();
	  } catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  } catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
//	  curClients.remove(client);
  }
  
	public synchronized int getRecordId() {
	  int id = recordId;
	  recordId++;
	  return id;
	}

	class TCPClient implements Runnable {
        BufferedReader sc;
		Socket socket;
		public TCPClient(Socket clientSocket) {
			socket = clientSocket;

		}

		public void run() {
			try {

				String command;

                PrintWriter pout = new PrintWriter(socket.getOutputStream());
                sc = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				while (true) {


                    String return_data = "";

                    while ((command = sc.readLine()) != null) {
                        Scanner commandSc = new Scanner(command);
                        return_data = executeCommand(commandSc);
                        pout.println(return_data);
                        pout.flush();
                        if (return_data.equals("exit")) {
                            break;
                        }
                    }
                    if (return_data.equals("exit")) {
                        break;
                    }
				}

			}
			catch (Exception e){

			}
		}
	}

}
class Student {
	String name;
	String book;
	int id;
	public Student(String n, String b, int id) {
		name = n;
		book = b;
		this.id = id;
	}
}