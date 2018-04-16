import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class BookServer {
	
	public static ConcurrentMap<String,AtomicInteger> inventory = new ConcurrentHashMap<String,AtomicInteger>();
	public static AtomicInteger nextrecordID = new AtomicInteger();
	public static ConcurrentMap<String,CopyOnWriteArrayList<Integer>> recordLookup = new ConcurrentHashMap<String,CopyOnWriteArrayList<Integer>>();
	public static List<String> record = new CopyOnWriteArrayList<String>();
	
	public static class UDPServer implements Runnable {
		
		private DatagramSocket socket;
		private InetAddress address;
		private int port;
		
		UDPServer(String message, InetAddress clientAddress, int clientPort) {
			try {
				if (!message.equals("test")) {
					throw new IllegalArgumentException();
				}
				socket = new DatagramSocket();
				address = clientAddress;
				port = clientPort;
				byte[] outbuf = "routing".getBytes();
				DatagramPacket outpacket = new DatagramPacket(outbuf, outbuf.length, this.address, this.port);
				socket.send(outpacket);
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				System.out.println(e); // debugging purposes
			}
		}

		@Override
		public void run() {
			int len = 1024;
			byte[] inbuf = new byte[len];
			byte[] outbuf = null;
			DatagramPacket inpacket, outpacket;		
			try {
				ByteArrayInputStream cmdStream;
				ObjectInputStream inObj;
				while (true) {
					inpacket = new DatagramPacket(inbuf, inbuf.length);
					socket.receive(inpacket);
					cmdStream = new ByteArrayInputStream(inpacket.getData());
					inObj = new ObjectInputStream(cmdStream);
					String[] command = (String[])inObj.readObject();
					inObj.close();
					if (command[0].equals("setmode")) {
						outbuf = "switching".getBytes();
						outpacket = new DatagramPacket(outbuf, outbuf.length, this.address, this.port);
						socket.send(outpacket);
						return;
					} else if (command[0].equals("borrow")) {
						if(inventory.containsKey(command[2])) {
							AtomicInteger quantity = inventory.get(command[2]);
							int value;
							do {
								value = quantity.get();
							} while (value > 0 && !quantity.compareAndSet(value, --value));
							if (value == 0) {
								outbuf = "Request Failed - Book not available".getBytes();
							}
							else {
								synchronized (nextrecordID) {
									record.add(command[2]);
									value = nextrecordID.incrementAndGet();
								}	
								List<Integer> ids = recordLookup.putIfAbsent(command[1], new CopyOnWriteArrayList<Integer>());
								ids.add(value);
								outbuf = ("Your request has been approved, " + value + " " + command[1] + " \""+ command[2] + "\"").getBytes();
							}
						}
						else {
							outbuf = "Request Failed - We do not have this book".getBytes();
						}
						
					} else if (command[0].equals("return")) {
						try {
							Integer value = Integer.parseInt(command[1]);
							String bookname = record.get(value-1);
							inventory.get(bookname).incrementAndGet();
							for (Entry<String,CopyOnWriteArrayList<Integer>> pair: recordLookup.entrySet()) {
								CopyOnWriteArrayList<Integer> ids = pair.getValue();
								synchronized (ids) {
									if (ids.contains(value)) {
										 ids.remove(value);
										 outbuf = (command[1] + " is returned").getBytes();
									 }
								} 
							}
							if (outbuf == null) {
								outbuf = (command[1] + " not found, no such borrow record").getBytes();
							}
						} catch (IndexOutOfBoundsException e) {
							outbuf = (command[1] + " not found, no such borrow record").getBytes();
						}
					} else if (command[0].equals("inventory")) {
						String message = "";
						for (Entry<String, AtomicInteger> pair: inventory.entrySet()) {
							message += "\"" + pair.getKey() + "\" " + pair.getValue() + System.lineSeparator();
						}
						outbuf = message.substring(0, message.length()-1).getBytes();
					} else if (command[0].equals("list")) {
						if (recordLookup.containsKey(command[1])) {
							CopyOnWriteArrayList<Integer> ids = recordLookup.get(command[1]);
							synchronized (ids) {
								if (ids.isEmpty()) {
									outbuf = ("No record found for " + command[1]).getBytes();
								}
								else {
									String message = "";
									for (Integer id: ids) {
										message += id + " \"" + record.get(id-1) + "\"" + System.lineSeparator();
									}
									outbuf = message.substring(0, message.length()-1).getBytes();
								}
							}	
						}
						else {
							outbuf = ("No record found for " + command[1]).getBytes();
						}
						
					} else if (command[0].equals("exit")) {
						String message = "";
						for (Entry<String, AtomicInteger> pair: inventory.entrySet()) {
							message += "\"" + pair.getKey() + "\" " + pair.getValue() + System.lineSeparator();
						}
						outbuf = message.substring(0, message.length()-1).getBytes();
						outpacket = new DatagramPacket(outbuf, outbuf.length, this.address, this.port);
						socket.send(outpacket);
						return;
					} else {
						System.out.println("ERROR: No such command"); // debugging purposes
					}
					// send result
					outpacket = new DatagramPacket(outbuf, outbuf.length, this.address, this.port);
					socket.send(outpacket);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				System.out.println(e); // debugging purposes
			} catch (ClassNotFoundException e) {
				System.out.println(e); // debugging purposes
			}
		}
		
	}
	
	public static class TCPServer implements Runnable {
		
		private Socket socket;
		private InputStream reader;
		private OutputStream writer;
		
		TCPServer(Socket clientSocket) {
			socket = clientSocket;
			try {
				reader = socket.getInputStream();
				writer = socket.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}

		@Override
		public void run() {
			try {
				ByteArrayInputStream cmdStream;
				ObjectInputStream inObj;
				while (true) {
					int len = 1024;
					byte[] inbuf = new byte[len];
					byte[] outbuf = null;
					reader.read(inbuf, 0, inbuf.length);
					cmdStream = new ByteArrayInputStream(inbuf);
					inObj = new ObjectInputStream(cmdStream);
					String[] command = (String[])inObj.readObject();
					inObj.close();
					if (command[0].equals("setmode")) {
						outbuf = "switching".getBytes();
						writer.write(outbuf, 0, outbuf.length);
						writer.flush();
						return;
					} else if (command[0].equals("borrow")) {
						if(inventory.containsKey(command[2])) {
							AtomicInteger quantity = inventory.get(command[2]);
							int value;
							do {
								value = quantity.get();
							} while (value > 0 && !quantity.compareAndSet(value, value-1));
							if (value == 0) {
								outbuf = "Request Failed - Book not available".getBytes();
							}
							else {
								synchronized (record) {
									record.add(command[2]);
									value = nextrecordID.incrementAndGet();
								}	
								recordLookup.putIfAbsent(command[1], new CopyOnWriteArrayList<Integer>());
								List<Integer> ids = recordLookup.get(command[1]);
								ids.add(value);
								outbuf = ("Your request has been approved, " + value + " " + command[1] + " \""+ command[2] + "\"").getBytes();
							}
						}
						else {
							outbuf = "Request Failed - We do not have this book".getBytes();
						}
						
					} else if (command[0].equals("return")) {
						try {
							Integer value = Integer.parseInt(command[1]);
							String bookname = record.get(value-1);
							inventory.get(bookname).incrementAndGet();
							for (Entry<String,CopyOnWriteArrayList<Integer>> pair: recordLookup.entrySet()) {
								CopyOnWriteArrayList<Integer> ids = pair.getValue();
								synchronized (ids) {
									if (ids.contains(value)) {
										 ids.remove(value);
										 outbuf = (command[1] + " is returned").getBytes();
										 break;
									 }
								} 
							}
							if (outbuf == null) {
								outbuf = (command[1] + " not found, no such borrow record").getBytes();
							}
						} catch (IndexOutOfBoundsException e) {
							outbuf = (command[1] + " not found, no such borrow record").getBytes();
						}

					} else if (command[0].equals("inventory")) {
						String message = "";
						for (Entry<String, AtomicInteger> pair: inventory.entrySet()) {
							message += "\"" + pair.getKey() + "\" " + pair.getValue() + System.lineSeparator();
						}
						outbuf = message.substring(0, message.length()-1).getBytes();
					} else if (command[0].equals("list")) {
						if (recordLookup.containsKey(command[1])) {
							CopyOnWriteArrayList<Integer> ids = recordLookup.get(command[1]);
							synchronized (ids) {
								if (ids.isEmpty()) {
									outbuf = ("No record found for " + command[1]).getBytes();
								}
								else {
									String message = "";
									for (Integer id: ids) {
										message += id + " \"" + record.get(id-1) + "\"" + System.lineSeparator();
									}
									outbuf = message.substring(0, message.length()-1).getBytes();
								}
							}	
						}
						else {
							outbuf = ("No record found for " + command[1]).getBytes();
						}
						
					} else if (command[0].equals("exit")) {
						String message = "";
						for (Entry<String, AtomicInteger> pair: inventory.entrySet()) {
							message += "\"" + pair.getKey() + "\" " + pair.getValue() + System.lineSeparator();
						}
						outbuf = message.substring(0, message.length()-1).getBytes();
						writer.write(outbuf, 0, outbuf.length);
						writer.flush();
						return;
					} else {
						System.out.println("ERROR: No such command"); // debugging purposes
					}
					// send result
					writer.write(outbuf, 0, outbuf.length);
					writer.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.out.println(e); // debugging purposes
			}
			
		}
		
	}
	
	public static class UDPConnector implements Runnable {
		
		private int port;
		
		UDPConnector(int port) {
			this.port = port;
		}

		@Override
		public void run() {
			int len = 1024;
			try {
				DatagramSocket datasocket = new DatagramSocket(port);
				byte[] buf = new byte[len];
				while (true) {
					DatagramPacket datapacket = new DatagramPacket(buf, buf.length);
					datasocket.receive(datapacket);
					(new Thread(new UDPServer(new String(datapacket.getData(),0,datapacket.getLength()),datapacket.getAddress(),datapacket.getPort()))).start();
				}
			} catch (SocketException e) {
				System.err.println(e);
			} catch (IOException e) {
				System.err.println(e);
			}	
		}
		
	}
	
	public static class TCPConnector implements Runnable {
		
		private int port;
		
		TCPConnector(int port) {
			this.port = port;
		}

		@Override
		public void run() {
			ServerSocket serverSocket;
			try {
				serverSocket = new ServerSocket(port);
				while(true) {
					Socket socket = serverSocket.accept();
					(new Thread(new TCPServer(socket))).start();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
	}
	
	public static void main (String[] args) {
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
		Scanner sc;
		try {
			FileReader freader = new FileReader(fileName);
			sc = new Scanner(freader);
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] tokens = line.split("\"");
				if (tokens.length < 3) {
					throw new NumberFormatException();
				}
				inventory.put(tokens[1],new AtomicInteger(Integer.parseInt(tokens[2].trim())));
			}
			sc.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found"); // debugging purposes
			return;
		} catch (NumberFormatException e) {
			System.out.println("Invalid input format"); // debugging purposes
			return;
		}
		// handle request from clients
		(new Thread(new TCPConnector(tcpPort))).start();
		(new Thread(new UDPConnector(udpPort))).start();
  	}
}
