import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class BookServer {
	static LinkedHashMap<String, Integer> inventory = null;
	//record_id, [book_name, student_name]
	static HashMap<Integer, String[]> records = null;
	//student_name, list of record_id's
	static HashMap<String, ArrayList<Integer>> student_records = null;
	static int record_id = 0;
	
	public static void main (String[] args) {
		//args = new String[]{"inventory.txt"};
		int tcpPort;
		int udpPort;
		if (args.length != 1) {
			System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
			System.exit(-1);
		}
		String fileName = args[0];
		tcpPort = 7000;
		udpPort = 8000;

		records = new HashMap<Integer, String[]>();
		student_records = new HashMap<String, ArrayList<Integer>>();
		// parse the inventory file
		inventory = new LinkedHashMap<String, Integer>();
		String inventoryInput = fileToString(fileName);
		String[] lines = inventoryInput.split("\\r?\\n");
		for (String s : lines) {
			String[] info = s.split("\"");
			//System.out.println("adding " + info[1] + " with quantity " + info[2]);
			inventory.put(info[1], Integer.parseInt(info[2].substring(1)));
		}

		// This thread handles creation of TCP client connections. 
		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					//System.out.println("TCP setup starting");
					@SuppressWarnings("resource")
					ServerSocket serverSocket = new ServerSocket(tcpPort);
					while (true) {
						// Makes a new thread for each client connection
						Socket clientSocket = serverSocket.accept();
						PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
						//Object obj = clientSocket.getOutputStream();
						
						new Thread(new Runnable(){
							@Override
							public void run() {
								String message;
								try {
									/*ObjectInputStream reader = new ObjectInputStream(
											new BufferedInputStream(clientSocket.getInputStream()));*/
									BufferedReader reader = new BufferedReader(
											new InputStreamReader(clientSocket.getInputStream()));
									// Periodically checks for messages
									while ((message = reader.readLine()) != null) {
										String response = processMessage(message);
										if (response.equals("exit")) {
											writer.close();
											reader.close();
											clientSocket.close();
											break;
										} else {
											writer.println(response);
											writer.flush();
										}
									}
								} catch (SocketException e) {
									//System.out.println("Client disconnected!");
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}).start();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
        // This thread handles creation of UDP client connections.
        new Thread(new Runnable(){
            @Override
            public void run() {
                DatagramPacket datapacket, returnpacket;
                int len = 1024;
                try {
                    //System.out.println("UDP setup starting");
                    @SuppressWarnings("resource")
                    DatagramSocket datasocket = new DatagramSocket(udpPort);
                    byte[] buf = new byte[len];
                    byte[] outBuf = new byte[len];
                    while (true) {
                        datapacket = new DatagramPacket(buf, buf.length);
                        datasocket.receive(datapacket);
                        String msg = new String(datapacket.getData(), 
                                datapacket.getOffset(), datapacket.getLength());
                        String output = processMessage(msg);
                        //if output.equals("exit") then stop this stuff
                        outBuf = output.getBytes();
                        returnpacket = new DatagramPacket(outBuf, outBuf.length,
                                datapacket.getAddress(), datapacket.getPort());
                        datasocket.send(returnpacket);
                        //System.out.println("Message sent");
                    }
                } catch (SocketException e) {
                    System.err.println(e);
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }).start();
		
	}
	
	public static synchronized String processMessage(String message) {
		//System.out.println(message);
		String[] tokens = message.split(" ");
		
		if (tokens[0].equals(("borrow"))) {
			String student_name = tokens[1];
			String book_name = message.split("\"")[1];
			//do record-id later
			if (!inventory.containsKey(book_name)) {
				//System.out.println("dont have book");
				return "Request Failed - We do not have this book.";
			} else if (inventory.get(book_name).equals(0)) {
				//System.out.println("book not avail");
				return "Request Failed - Book not available";
			} else {
				inventory.put(book_name, inventory.get(book_name) - 1);
				//add to our request records
				String[] book_student = new String[]{book_name, student_name};
				records.put(++record_id, book_student);
				//String record_msg = record_id + " \"" + book_name + "\"";
				if (student_records.containsKey(student_name)) {
					ArrayList<Integer> recs = student_records.get(student_name);
					recs.add(record_id);
					student_records.put(student_name, recs);
				} else {
					ArrayList<Integer> recs = new ArrayList<Integer>();
					recs.add(record_id);
					student_records.put(student_name, recs);
				}
				//System.out.println("req approved");
				return "Your request has been approved, " + record_id + " " + student_name + " \"" + book_name + "\"";
			}
		} else if (tokens[0].equals("return")) {
			Integer rec_id = Integer.parseInt(tokens[1]);
			if (records.containsKey(rec_id)) {
				String book_name = records.get(rec_id)[0];
				String stud_name = records.get(rec_id)[1];
				records.remove(rec_id);
				ArrayList<Integer> stud_recs = student_records.get(stud_name);
				for (Integer i : stud_recs) {
					if (i.equals(rec_id)) {
						stud_recs.remove(rec_id);
						break;
					}
				}
				student_records.put(stud_name, stud_recs);
				inventory.put(book_name, inventory.get(book_name) + 1);
				return tokens[1] + " is returned";
			} else {
				return tokens[1] + " not found, no such borrow record";
			} 
		} else if (tokens[0].equals("list")) {
			if (!student_records.containsKey(tokens[1])) {
				return "No record found for " + tokens[1];
			} else {
				ArrayList<Integer> stud_recs = student_records.get(tokens[1]);
				if (stud_recs.isEmpty()) {
					return "No record found for " + tokens[1];
				} else {
					String list;
					list = Integer.toString(stud_recs.get(0)) + " \"" + records.get(stud_recs.get(0))[0] + "\"";
					for (int i = 1; i < stud_recs.size(); i++) {
						list += "&carrot@!666" + Integer.toString(stud_recs.get(i)) + " \"" + records.get(stud_recs.get(0))[0] + "\"";
					}
					return list;
				}
			}
		} else if (tokens[0].equals("inventory")) {
			String prefix = "";
			String list = "";
			String book_name;
			String book_count;
			for (String key : inventory.keySet()) {
				book_name = "\"" + key + "\"";
				book_count = Integer.toString(inventory.get(key));
				list += prefix + book_name + " " + book_count;
				prefix = "&carrot@!666";
			}
			return list;
		} else if (tokens[0].equals("exit")) {
			String prefix = "";
			String list = "";
			String book_name;
			String book_count;
			for (String key : inventory.keySet()) {
				book_name = "\"" + key + "\"";
				book_count = Integer.toString(inventory.get(key));
				list += prefix + book_name + " " + book_count;
				prefix = "\n";
			}
			printToFile("src/inventory.txt", list);
			return "exit";
		}
		return "";
	}
	
	
    public static String fileToString(String resource) {
    	try(BufferedReader br = new BufferedReader(new FileReader("src/" + resource))) {
    	    StringBuilder sb = new StringBuilder();
    	    String line = br.readLine();

    	    while (line != null) {
    	        sb.append(line);
    	        sb.append(System.lineSeparator());
    	        line = br.readLine();
    	    }
    	    br.close();
    	    return sb.toString();
    	} catch(Exception e) {
    		return "";
    	}
    }
    
    public static void printToFile(String resource, String str) {
    		try {
			FileWriter fw = new FileWriter(resource);
    			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(str);
			if (bw!=null) bw.close();
			if (fw!=null) fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
}
