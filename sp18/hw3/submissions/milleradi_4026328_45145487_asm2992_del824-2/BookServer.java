import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;
public class BookServer {
	
	
	static HashMap<String, Integer> inventory= new HashMap<String, Integer>();
	static Semaphore inventoryLock= new Semaphore(1);
	
	static HashMap<Integer, String> borrowRecords= new HashMap<Integer, String>();
	static Semaphore recordLock= new Semaphore(1);
	
	static HashMap<String, HashMap<Integer,String>> studentRecord= new HashMap<String, HashMap<Integer,String>>();
	static Semaphore studentLock= new Semaphore(1);
	
	static int recordID =1 ;
	static Semaphore recordIDLock = new Semaphore(1);
	
	
	  @SuppressWarnings("resource")
	public static void main (String[] args) throws Exception{
	    int tcpPort;
	    int udpPort;
	    if (args.length != 1) {
	      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
	      System.exit(-1);
	    }
	    String fileName = args[0];
	    tcpPort = 7000;
	    udpPort = 8000;

	    // parse the inventory file
	    Scanner sc = new Scanner(new FileReader(fileName));
	    inventoryLock.acquire();
	    while(sc.hasNextLine()) {
	    	
	    	String entry = sc.nextLine();
	    	String [] tok = entry.split("\" ");
	    	tok[0] = tok[0]+"\"";
	    	tok[1] = tok[1].replaceAll("\\s+","");
	    	System.out.println(tok[0]+tok[1]);
	    	inventory.put(tok[0], Integer.parseInt(tok[1]));
	    	System.out.println(inventory);
	    	
	    }
	    inventoryLock.release();
	    // TODO: handle request from clients
			DatagramSocket datasocket = new DatagramSocket(udpPort);
			DatagramPacket datapacket;
			byte[] buf = new byte[1024];
			
			ServerSocket serverSocket = new ServerSocket(7000);
			Socket socket=new Socket();
			while (true) {
				buf=null;
				buf=new byte[1024];
				datapacket = new DatagramPacket(buf, buf.length);
				datasocket.receive(datapacket);
				socket = serverSocket.accept();
				//datasocket.close();
				String cmd = new String(datapacket.getData());
				int returnport = datapacket.getPort();
				String[] message=cmd.split(" ");
				String id=message[message.length-1];
				id=id.trim();
				InetAddress returnaddress = datapacket.getAddress();
				udpHandler udp= new udpHandler(cmd, returnport, returnaddress,Integer.parseInt(id),udpPort);
				try {
					serverSocket = new ServerSocket(7000);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					socket = serverSocket.accept();
				}catch(Exception e){
					
				}

				tcpHandler tcp = new tcpHandler(cmd,socket,Integer.parseInt(id));
			
				Thread t1= new Thread(udp);
				Thread t2= new Thread(tcp);
				t1.start();
				t2.start();
				//send to handler
			}

	  }

	private static Integer ParseInt(String string) {
		// TODO Auto-generated method stub
		return null;
	}
	}
 
 class udpHandler implements Runnable{
	private String command;
	private int returnPort;
	private InetAddress returnAddress;
	private int clientID;
	private int UDPport;
	udpHandler(String cmd, int port, InetAddress address,int id, int UDP) throws UnknownHostException{
		command=cmd;
		returnPort=port;
		returnAddress=InetAddress.getByName("localhost");;
		clientID=id;
		UDPport=UDP;
		
	}
	 @Override
	public void run() {
		// TODO Auto-generated method stub
		 DatagramPacket returnpacket;
		 byte[] sendbuf = new byte[1024];
		 DatagramSocket datasocket = null;
		try {
			
			datasocket = new DatagramSocket(8000 + clientID);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		};
         String[] tokens = command.split(" ");
         
       if (tokens[0].equals("borrow")) {
           // TODO: send appropriate command to the server and display the
           // appropriate responses form the server
    	   System.out.println("in borrow function");
       	String response="";
       	String[] newTok=command.split("\"");
       	String[] split = newTok[0].split(" ");
       	newTok[2]="\""+newTok[1]+"\"";//bookname
       	newTok[1]=split[1];//student name
       	newTok[0]=split[0];// command "borrow"
       	try {
       		System.out.println(BookServer.inventoryLock.availablePermits());
       		System.out.println(BookServer.studentLock.availablePermits());
       		System.out.println(BookServer.recordLock.availablePermits());
       		System.out.println(BookServer.recordIDLock.availablePermits());
			BookServer.inventoryLock.acquire();
			BookServer.studentLock.acquire();
	       	BookServer.recordLock.acquire();
	       	BookServer.recordIDLock.acquire();
	       
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			BookServer.inventoryLock.release();
			BookServer.studentLock.release();
	       	BookServer.recordLock.release();
	       	BookServer.recordIDLock.release();
			e.printStackTrace();
		}
       	if(BookServer.inventory.containsKey(newTok[2])){
       		System.out.println("stuck here3");
       		for(String book: BookServer.inventory.keySet()){
       			if(book.equals(newTok[2])){
       				System.out.println(BookServer.inventory.get(book));
       				if(BookServer.inventory.get(book)==0){
       					response="Request Failed - Book not available"+"\n";
       				}
       				else{
       					
       					response="Your request has been approved, "+BookServer.recordID+" "+newTok[1]+" "+newTok[2]+"\n";
       					BookServer.borrowRecords.put(BookServer.recordID, newTok[2]);
       					if(BookServer.studentRecord.containsKey(newTok[1])){
       						HashMap<Integer,String> current_record=BookServer.studentRecord.get(newTok[1]);
       						current_record.put(BookServer.recordID, newTok[2]);
       						BookServer.studentRecord.put(newTok[1], current_record);
       					}
       					else{
       						HashMap<Integer, String> new_record= new HashMap<Integer, String>();
       						new_record.put(BookServer.recordID, newTok[2]);
       						BookServer.studentRecord.put(newTok[1], new_record);
       					}
       					int num_books=BookServer.inventory.get(newTok[2]);
       					num_books--;
       					BookServer.inventory.put(newTok[2], num_books);
       					BookServer.recordID++;
       				}
       			}
       		}
       	}
       	else{
       		response="Request Failed - We do not have this book"+"\n";
       	}
       		BookServer.inventoryLock.release();
			BookServer.studentLock.release();
			BookServer.recordLock.release();
			BookServer.recordIDLock.release();
       		System.out.println(response);
			sendbuf=response.getBytes();
			returnpacket = new DatagramPacket(sendbuf,sendbuf.length,returnAddress,returnPort);
			try {
				datasocket.send(returnpacket);
				System.out.println("sent book not available message");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
       } else if (tokens[0].equals("return")) {
           // TODO: send appropriate command to the server and display the
           // appropriate responses form the server\
       	String response="";
       	int recordNum=Integer.parseInt(tokens[1].trim());
       	try {
			BookServer.recordLock.acquire();
			
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			BookServer.recordLock.release();
		}
       	String record=BookServer.borrowRecords.get(recordNum);
       	if(record==null) response = tokens[1]+" not found, no such borrow record"+"\n";
       	else{
       		for(String book: BookServer.inventory.keySet()){
       			if(book.equals(record)){
       				try {
						BookServer.inventoryLock.acquire();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						BookServer.inventoryLock.release();
					}
       				int num=BookServer.inventory.get(book)+1;
       				BookServer.inventory.put(book, num);
           			response = tokens[1].trim()+" is returned"+"\n";
           			
       			}
       		}
       		BookServer.recordLock.release();
   			BookServer.inventoryLock.release();
       	}
       	sendbuf=response.getBytes();
       	returnpacket = new DatagramPacket(sendbuf,sendbuf.length,returnAddress,returnPort);
		try {
			datasocket.send(returnpacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
       } else if (tokens[0].trim().equals("inventory")) {
    	   
       	String response="";
           // TODO: send appropriate command to the server and display the
           // appropriate responses form the server
       	try {
			BookServer.inventoryLock.acquire();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			BookServer.inventoryLock.release();
		}
       	for(String book: BookServer.inventory.keySet()){
       		response+=book+" "+BookServer.inventory.get(book).toString()+"\n";
       	}
       	BookServer.inventoryLock.release();
       	sendbuf=response.getBytes();
       	returnpacket = new DatagramPacket(sendbuf,sendbuf.length,returnAddress,returnPort);
		try {
			datasocket.send(returnpacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
       } else if (tokens[0].equals("list")) {
    	   
    	   /*– list all books borrowed by the student. If no borrow record is found
    	   for the student, the system responds with a message: ‘No record found for <student-name>’.
    	   Otherwise, list all records of the student as <record-id> <book-name>. Note that, you should
    	   print one line per borrow record.*/
    	   
       	String response="";
           // TODO: send appropriate command to the server and display the
           // appropriate responses form the server
       	try {
			BookServer.studentLock.acquire();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
       	if(BookServer.studentRecord.containsKey(tokens[1])){
       		for(String student: BookServer.studentRecord.keySet()){
       			if(student.equals(tokens[1])){
       				for(Integer id:BookServer.studentRecord.get(student).keySet()){
       					response+= id.toString()+" "+ BookServer.studentRecord.get(student).get(id)+"\n";
       				}
       			}
       		}

       	}
       	else{
       		response="No record found for "+tokens[1]+"\n";
       	}
       	BookServer.studentLock.release();
       		sendbuf=response.getBytes();
       		returnpacket = new DatagramPacket(sendbuf,sendbuf.length,returnAddress,returnPort);
			try {
				datasocket.send(returnpacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
       } else if (tokens[0].equals("exit")) {
//   	    File inventory=new File("inventory.txt");
//   	    FileWriter fw = new FileWriter(inventory);
//   	    for(int i=0;i<library.size();i++) {
//   	    	String entry = library.;
//   	    	if(sc.hasNextLine()) entry=entry+'\n';
//   	    	fw.write(entry);
//   	    }
//   	    fw.close();
    	   	String response = "0";
    	   	sendbuf=response.getBytes();
       		returnpacket = new DatagramPacket(sendbuf,sendbuf.length,returnAddress,returnPort);
			try {
				datasocket.send(returnpacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String fileName = "inventory.txt";
			File file = new File(fileName);
			PrintWriter poutFile=null;
			try {
				poutFile = new PrintWriter(new FileWriter(file));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String text="";
			for(String book: BookServer.inventory.keySet()){
				text+=book+" "+BookServer.inventory.get(book)+"\n";
			}
			poutFile.println(text);
			poutFile.flush();
			return;
   	}
       datasocket.close();
		
	}
	
}
 
 class tcpHandler implements Runnable{
	
		private String command;
		private Socket socket;
		private PrintWriter pout;
		private Scanner in;
		private int clientID;
		tcpHandler(String cmd, Socket data, int id) throws IOException{
			command=cmd;
			socket = data;
			pout = new PrintWriter(socket.getOutputStream());
			in = new Scanner(socket.getInputStream());
			clientID=id;
			
		}
		 @Override
		public void run() {
			// TODO Auto-generated method stub
			 
	         String[] tokens = command.split(" ");
	         
	       if (tokens[0].equals("borrow")) {
	           // TODO: send appropriate command to the server and display the
	           // appropriate responses form the server
	    	   System.out.println("in borrow function");
	       	String response="";
	       	String[] newTok=command.split("\"");
	       	String[] split = newTok[0].split(" ");
	       	newTok[2]="\""+newTok[1]+"\"";//bookname
	       	newTok[1]=split[1];//student name
	       	newTok[0]=split[0];// command "borrow"
	       	try {
	       		System.out.println(BookServer.inventoryLock.availablePermits());
	       		System.out.println(BookServer.studentLock.availablePermits());
	       		System.out.println(BookServer.recordLock.availablePermits());
	       		System.out.println(BookServer.recordIDLock.availablePermits());
				BookServer.inventoryLock.acquire();
				BookServer.studentLock.acquire();
		       	BookServer.recordLock.acquire();
		       	BookServer.recordIDLock.acquire();
		       
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				BookServer.inventoryLock.release();
				BookServer.studentLock.release();
		       	BookServer.recordLock.release();
		       	BookServer.recordIDLock.release();
				e.printStackTrace();
			}
	       	if(BookServer.inventory.containsKey(newTok[2])){
	       		System.out.println("stuck here3");
	       		for(String book: BookServer.inventory.keySet()){
	       			if(book.equals(newTok[2])){
	       				System.out.println(BookServer.inventory.get(book));
	       				if(BookServer.inventory.get(book)==0){
	       					response="Request Failed - Book not available"+"\n";
	       				}
	       				else{
	       					
	       					response="Your request has been approved, "+BookServer.recordID+" "+newTok[1]+" "+newTok[2]+"\n";
	       					BookServer.borrowRecords.put(BookServer.recordID, newTok[2]);
	       					if(BookServer.studentRecord.containsKey(newTok[1])){
	       						HashMap<Integer,String> current_record=BookServer.studentRecord.get(newTok[1]);
	       						current_record.put(BookServer.recordID, newTok[2]);
	       						BookServer.studentRecord.put(newTok[1], current_record);
	       					}
	       					else{
	       						HashMap<Integer, String> new_record= new HashMap<Integer, String>();
	       						new_record.put(BookServer.recordID, newTok[2]);
	       						BookServer.studentRecord.put(newTok[1], new_record);
	       					}
	       					int num_books=BookServer.inventory.get(newTok[2]);
	       					num_books--;
	       					BookServer.inventory.put(newTok[2], num_books);
	       					BookServer.recordID++;
	       				}
	       			}
	       		}
	       	}
	       	else{
	       		response="Request Failed - We do not have this book"+"\n";
	       	}
	       		BookServer.inventoryLock.release();
				BookServer.studentLock.release();
				BookServer.recordLock.release();
				BookServer.recordIDLock.release();
	       		System.out.println(response);
	       		pout.println(response);
	    		pout.flush();
	    	
	       } else if (tokens[0].equals("return")) {
	           // TODO: send appropriate command to the server and display the
	           // appropriate responses form the server\
	       	String response="";
	       	int recordNum=Integer.parseInt(tokens[1].trim());
	       	try {
				BookServer.recordLock.acquire();
				
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				BookServer.recordLock.release();
			}
	       	String record=BookServer.borrowRecords.get(recordNum);
	       	if(record==null) response = tokens[1]+" not found, no such borrow record"+"\n";
	       	else{
	       		for(String book: BookServer.inventory.keySet()){
	       			if(book.equals(record)){
	       				try {
							BookServer.inventoryLock.acquire();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							BookServer.inventoryLock.release();
						}
	       				int num=BookServer.inventory.get(book)+1;
	       				BookServer.inventory.put(book, num);
	           			response = tokens[1].trim()+" is returned"+"\n";
	           			
	       			}
	       		}
	       		BookServer.recordLock.release();
	   			BookServer.inventoryLock.release();
	       	}
	    	pout.println(response);
			pout.flush();
		
			
	       } else if (tokens[0].trim().equals("inventory")) {
	    	   
	       	String response="";
	           // TODO: send appropriate command to the server and display the
	           // appropriate responses form the server
	       	try {
				BookServer.inventoryLock.acquire();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				BookServer.inventoryLock.release();
			}
	       	for(String book: BookServer.inventory.keySet()){
	       		response+=book+" "+BookServer.inventory.get(book).toString()+"\n";
	       	}
	       	BookServer.inventoryLock.release();
	    	pout.println(response);
			pout.flush();
			
	       } else if (tokens[0].equals("list")) {
	    	   
	    	   /*– list all books borrowed by the student. If no borrow record is found
	    	   for the student, the system responds with a message: ‘No record found for <student-name>’.
	    	   Otherwise, list all records of the student as <record-id> <book-name>. Note that, you should
	    	   print one line per borrow record.*/
	    	   
	       	String response="";
	           // TODO: send appropriate command to the server and display the
	           // appropriate responses form the server
	       	try {
				BookServer.studentLock.acquire();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	       	if(BookServer.studentRecord.containsKey(tokens[1])){
	       		for(String student: BookServer.studentRecord.keySet()){
	       			if(student.equals(tokens[1])){
	       				for(Integer id:BookServer.studentRecord.get(student).keySet()){
	       					response+= id.toString()+" "+ BookServer.studentRecord.get(student).get(id)+"\n";
	       				}
	       			}
	       		}

	       	}
	       	else{
	       		response="No record found for "+tokens[1]+"\n";
	       	}
	       	BookServer.studentLock.release();
	    	pout.println(response);
			pout.flush();
			
				
	       } else if (tokens[0].equals("exit")) {

	    	   	String response = "0";
	    		pout.println(response);
	    		pout.flush();
	    		
				
				String fileName = "inventory.txt";
				File file = new File(fileName);
				PrintWriter poutFile=null;
				try {
					poutFile = new PrintWriter(new FileWriter(file));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String text="";
				for(String book: BookServer.inventory.keySet()){
					text+=book+" "+BookServer.inventory.get(book)+"\n";
				}
				poutFile.println(text);
				poutFile.flush();
				return;
	   	}
	      
			
		}
 }