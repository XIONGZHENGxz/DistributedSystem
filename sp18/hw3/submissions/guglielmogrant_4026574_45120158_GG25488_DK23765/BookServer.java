import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class BookServer {
	static ReentrantLock lock = new ReentrantLock();
	static ArrayList<Book> Lib = new ArrayList<Book>();
	static ArrayList<BookHolder> LibClients = new ArrayList<BookHolder>();
	static int id = 0;
	static int tcpPort = 7000;
	static int udpPort = 8000;
	
	public class TCPServerToClient implements Runnable{
		Socket socket;
		
		public TCPServerToClient(Socket s){
			socket = s;
		}
		
		@Override
		public void run() {
			try {
				BufferedReader input  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
				String message;
				while ((message = input.readLine()) != null) {
					String[] commands = message.split(" ", 3);
					lock.lock();
					if(commands[0].equals("exit")) {
						File file = new File("inventory.txt");
				        PrintWriter writer = new PrintWriter(file);
				        for(Book b : Lib) {
				        	writer.println(b.name + " " + b.checked_in);
				        }
				        writer.flush();
				        writer.close();
						lock.unlock();
						break;
					}
					else if(commands[0].equals("inventory")) {
						for(Book b : Lib) {
							output.println(b.name + " " + b.checked_in);
						}
						output.println("EOF");
					}
					else if(commands[0].equals("return")) {
						Book book = null;
						for(BookHolder bh : LibClients) {
							for(Book b : bh.books) {
								if(b.id == Integer.parseInt(commands[1])) {
									book = b;
									bh.books.remove(book);
									break;
								}
							}
							if(book != null)
								break;
						}
						if(book == null) {
							output.println(commands[1] + " not found, no such borrow record");
						}
						else {
							for(Book b : Lib) {
								if(b.name.equals(book.name)) {
									b.checked_in++;
								}
							}
							output.println(commands[1] + " is returned");
						}
					}
					else if(commands[0].equals("list")) {
						BookHolder student = null;
						for(BookHolder bh : LibClients) {
							if(bh.name.equals(commands[1])) {
								student = bh;
								break;
							}
						}
						if(student == null) {
							student = new BookHolder(commands[1]);
							LibClients.add(student);
						}
						if(student.books.size() == 0) {
							output.println("No record found for " + commands[1]);
						}
						else {
							for(Book b : student.books) {
								output.println(b.id + " " + b.name);
							}
						}
						output.println("EOF");
					}
					else if(commands[0].equals("borrow")) {
						BookHolder student = null;
						for(BookHolder bh : LibClients) {
							if(bh.name.equals(commands[1])) {
								student = bh;
								break;
							}
						}
						if(student == null) {
							student = new BookHolder(commands[1]);
							LibClients.add(student);
						}
						Book book = null;
						for(Book b : Lib) {
							if(b.name.endsWith(commands[2])) {
								book = b;
								break;
							}
						}
						if(book == null) {
							output.println("Request Failed: We do not have this book");
						}
						else if(book.checked_in == 0) {
							output.println("Request Failed: Book not available");
						}
						else {
							book.checked_in--;
							student.books.add(new Book(book.name, id));
							output.println("Your request has been approved, " + id + " " + commands[1] + " " + commands[2]);
							id++;
						}
					}
					else if(commands[0].equals("setmode")) {
						if(commands[1].equals("U")) {
							lock.unlock();
							break;
						}
					}
					lock.unlock();
			    }
				output.close();
				input.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public class TCPServer implements Runnable{
		@Override
		public void run() {
			try {
				@SuppressWarnings("resource")
				ServerSocket TCPLibSocket = new ServerSocket(tcpPort);
				while(true) {
					Socket newClient = TCPLibSocket.accept();
					(new Thread(new TCPServerToClient(newClient))).start();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public class UDPServer implements Runnable{
		@Override
		public void run() {
			try {
				@SuppressWarnings("resource")
				DatagramSocket UDPLibSocket = new DatagramSocket(udpPort);
				while(true) {
					byte[] rbuffer = new byte[1024];
					DatagramPacket rPacket = new DatagramPacket(rbuffer, rbuffer.length);
					UDPLibSocket.receive(rPacket);
					String[] commands = (new String(rPacket.getData(), 0, rPacket.getLength())).split(" ", 3);
					byte[] sbuffer;
					lock.lock();
					if(commands[0].equals("exit")) {
						File file = new File("inventory.txt");
				        PrintWriter writer = new PrintWriter(file);
				        for(Book b : Lib) {
				        	writer.println(b.name + " " + b.checked_in);
				        }
				        writer.flush();
				        writer.close();
					}
					else if(commands[0].equals("inventory")) {
						for(Book b : Lib) {
							sbuffer = (b.name + " " + b.checked_in).getBytes();
							DatagramPacket sPacket = new DatagramPacket(sbuffer, sbuffer.length, rPacket.getAddress(), rPacket.getPort());
							UDPLibSocket.send(sPacket);
							//output.println(b.name + " " + b.checked_in);
						}
						sbuffer = ("EOF").getBytes();
						DatagramPacket sPacket = new DatagramPacket(sbuffer, sbuffer.length, rPacket.getAddress(), rPacket.getPort());
						UDPLibSocket.send(sPacket);
						//output.println("EOF");
					}
					else if(commands[0].equals("return")) {
						Book book = null;
						for(BookHolder bh : LibClients) {
							for(Book b : bh.books) {
								if(b.id == Integer.parseInt(commands[1])) {
									book = b;
									bh.books.remove(book);
									break;
								}
							}
							if(book != null)
								break;
						}
						if(book == null) {
							sbuffer = (commands[1] + " not found, no such borrow record").getBytes();
							DatagramPacket sPacket = new DatagramPacket(sbuffer, sbuffer.length, rPacket.getAddress(), rPacket.getPort());
							UDPLibSocket.send(sPacket);
							//output.println(commands[1] + " not found, no such borrow record");
						}
						else {
							for(Book b : Lib) {
								if(b.name.equals(book.name)) {
									b.checked_in++;
								}
							}
							sbuffer = (commands[1] + " is returned").getBytes();
							DatagramPacket sPacket = new DatagramPacket(sbuffer, sbuffer.length, rPacket.getAddress(), rPacket.getPort());
							UDPLibSocket.send(sPacket);
							//output.println(commands[1] + " is returned");
						}
					}
					else if(commands[0].equals("list")) {
						BookHolder student = null;
						for(BookHolder bh : LibClients) {
							if(bh.name.equals(commands[1])) {
								student = bh;
								break;
							}
						}
						if(student == null) {
							student = new BookHolder(commands[1]);
							LibClients.add(student);
						}
						if(student.books.size() == 0) {
							sbuffer = ("No record found for " + commands[1]).getBytes();
							DatagramPacket sPacket = new DatagramPacket(sbuffer, sbuffer.length, rPacket.getAddress(), rPacket.getPort());
							UDPLibSocket.send(sPacket);
							//output.println("No record found for " + commands[1]);
						}
						else {
							for(Book b : student.books) {
								sbuffer = (b.id + " " + b.name).getBytes();
								DatagramPacket sPacket = new DatagramPacket(sbuffer, sbuffer.length, rPacket.getAddress(), rPacket.getPort());
								UDPLibSocket.send(sPacket);
								//output.println(b.id + " " + b.name);
							}
							sbuffer = ("EOF").getBytes();
							DatagramPacket sPacket = new DatagramPacket(sbuffer, sbuffer.length, rPacket.getAddress(), rPacket.getPort());
							UDPLibSocket.send(sPacket);
							//output.println("EOF");
						}
					}
					else if(commands[0].equals("borrow")) {
						BookHolder student = null;
						for(BookHolder bh : LibClients) {
							if(bh.name.equals(commands[1])) {
								student = bh;
								break;
							}
						}
						if(student == null) {
							student = new BookHolder(commands[1]);
							LibClients.add(student);
						}
						Book book = null;
						for(Book b : Lib) {
							if(b.name.endsWith(commands[2])) {
								book = b;
								break;
							}
						}
						if(book == null) {
							sbuffer = ("Request Failed: We do not have this book").getBytes();
							DatagramPacket sPacket = new DatagramPacket(sbuffer, sbuffer.length, rPacket.getAddress(), rPacket.getPort());
							UDPLibSocket.send(sPacket);
							//output.println("Request Failed: We do not have this book");
						}
						else if(book.checked_in == 0) {
							sbuffer = ("Request Failed: Book not available").getBytes();
							DatagramPacket sPacket = new DatagramPacket(sbuffer, sbuffer.length, rPacket.getAddress(), rPacket.getPort());
							UDPLibSocket.send(sPacket);
							//output.println("Request Failed: Book not available");
						}
						else {
							book.checked_in--;
							student.books.add(new Book(book.name, id));
							sbuffer = ("Your request has been approved, " + id + " " + commands[1] + " " + commands[2]).getBytes();
							DatagramPacket sPacket = new DatagramPacket(sbuffer, sbuffer.length, rPacket.getAddress(), rPacket.getPort());
							UDPLibSocket.send(sPacket);
							//output.println("Your request has been approved, " + id + " " + commands[1] + " " + commands[2]);
							id++;
						}
					}
					else if(commands[0].equals("setmode")) {
						if(commands[1].equals("T")) {
							
						}
					}
					lock.unlock();
				}
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main (String[] args) {
		if (args.length != 1) {
			System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
			System.exit(-1);
		}
		String fileName = args[0];

		// parse the inventory file
		try {
			FileReader input = new FileReader(fileName);
			BufferedReader bufRead = new BufferedReader(input);
			String myLine = null;
			while ( (myLine = bufRead.readLine()) != null)
			{    
				if(myLine.length() <= 1)
					break;
				int i = myLine.lastIndexOf(' ');
			    Lib.add(new Book(myLine.substring(0, i), Integer.parseInt(myLine.substring(i + 1))));
			}
			bufRead.close();
			input.close();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// TODO: handle request from clients
		BookServer bs = new BookServer();
		(new Thread(bs.new TCPServer())).start();
		(new Thread(bs.new UDPServer())).start();
  }
}
