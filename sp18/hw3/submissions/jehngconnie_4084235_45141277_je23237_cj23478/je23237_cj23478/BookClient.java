import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Random;
public class BookClient {
	private static final String EXIT = "exit";
	private static final String BORROW = "borrow";
	private static final String SETMODE = "setmode";
	private static final String RETURN = "return";
	private static final String LIST = "list";
	private static final String INVENTORY = "inventory";
	private File commandFile;
	private static final String EOM = "[EOM]";
	private int clientId;
    boolean isUDP = true;
    String hostAddress;
    int tcpPort;
    int udpPort;
   
    public static void main(String[] args){
    	 int clientId;
         File commandFile;
         if (args.length != 2) {
            System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
            System.out.println("\t(1) <command-file>: file with commands to the server");
            System.out.println("\t(2) client id: an integer between 1..9");
            //System.exit(-1); - default testing input params
            commandFile = new File("commandFile.txt");
        	Random r = new Random();
            clientId = r.nextInt(1000) + 1001;
        }
        else{
        	commandFile = new File(args[0]);
        	clientId = Integer.parseInt(args[1]);
        }
    	BookClient client = new BookClient(commandFile, clientId);
    	client.run();
    }
    public BookClient(File commandFile, int clientId){
    	this.commandFile = commandFile;
    	this.clientId = clientId;
    }
    public void run(){
        String command;
        Scanner sc;        
        hostAddress = "localhost";        
        tcpPort = 7000;
        udpPort = 8000;
        
        try {
            sc = new Scanner(new FileReader(commandFile));
            // clear out file if it exists
            PrintWriter writer = new PrintWriter("out_"+clientId+".txt");
            writer.print("");
            writer.close();
            while(sc.hasNextLine()) {
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");
                command = tokens[0].trim();
                
                if(EXIT.equalsIgnoreCase(command)){
                	exit();
                }
                else if(BORROW.equalsIgnoreCase(command)){
                	if(tokens.length >= 3){
                		String title = cmd.replace(tokens[0] + " " + tokens[1], " ");
                		borrow(tokens[1].trim(), title.trim());
                	}
                }
                else if(SETMODE.equalsIgnoreCase(command)){
                	if(tokens.length>=2)
                		setmode(tokens[1].trim());
                }
                else if(RETURN.equalsIgnoreCase(command)){
                	if(tokens.length>=2)
                		returnBook(tokens[1].trim());
                }
                else if(LIST.equalsIgnoreCase(command)){
                	if(tokens.length>=2)
                		list(tokens[1].trim());
                }
                else if(INVENTORY.equalsIgnoreCase(command)){
                	inventory();
                }
                else{
                	System.out.println("Command '"+command+"' is not understood");
                }                
            } // end while
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public void exit(){
    	System.out.println("EXITING.");
	    String response = sendCommand(EXIT);
	    try {
	    	BufferedWriter out = new BufferedWriter(new FileWriter("inventory.txt", false));
			out.write(response);
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public void borrow(String name, String title){
    	String command = "borrow " + name + " " + title;
    	String response = sendCommand(command);
    	try {
    		
	    	BufferedWriter out = new BufferedWriter(new FileWriter("out_"+clientId+".txt", true));		
			out.write(response + "\n");
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("FOR BORROW " +name + " " + title + ":\n" + response);
    }
    public void setmode(String mode){
    	if("T".equalsIgnoreCase(mode) && isUDP){
    		isUDP = false;
    	}
    	else if("U".equalsIgnoreCase(mode) && !isUDP){
    		isUDP = true;
    	}
    	System.out.println("SETMODE " + mode);
    	
    }
    public void returnBook(String recordId){
    	String command = "return " + recordId;
    	String response = sendCommand(command);
    	try {
	    	BufferedWriter out = new BufferedWriter(new FileWriter("out_"+clientId+".txt", true));		
			out.write(response + "\n");
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println(recordId + " IS RETURNING:\n" + response);
    }
    public void list(String name){
    	String command = "list " + name;
    	String bookList = sendCommand(command);
    	try {
	    	BufferedWriter out = new BufferedWriter(new FileWriter("out_"+clientId+".txt", true));	
			out.write(bookList + "\n");
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("LIST FOR " + name + " HAS:\n" + bookList);
    }
    public void inventory(){
    	String command = "inventory";
    	String bookList = sendCommand(command);
    	try {
	    	BufferedWriter out = new BufferedWriter(new FileWriter("out_"+clientId+".txt", true));		
			out.write(bookList + "\n");
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("INVENTORY HAS:\n" + bookList);
    }
    
 
    
    public String sendCommand(String netBuffer){
    	String retstring = null;
    	String curLine;
    	boolean keepReading = true;
    	try{
			DatagramSocket udpSocket;
		    Socket tcpSocket;
		    
    		if(!isUDP) {  //TCP
                try{
                	StringBuilder sb = new StringBuilder();
					tcpSocket = new Socket(hostAddress,tcpPort);
					PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);
                    out.println(netBuffer);
                    out.println(EOM);
                    out.flush();
                    System.out.println("Sending '"+netBuffer+"'");
                    BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
        	        while (keepReading && (curLine = in.readLine()) != null) {
        	        	System.out.println("Network '" + curLine + "'");
        	        	if(EOM.equals(curLine.trim()))
        	        		keepReading = false;
        	        	else{
	        	        	sb.append(curLine.trim());
	        	        	sb.append("\n");
        	        	}
        	        }
        	        retstring = sb.toString();
        	        //tcpSocket.close();
                }catch(ConnectException e){
                    System.out.println("server not available.");
                }catch(Exception e){
                    e.printStackTrace();
                }
            }else{ //UDP
                try {
                	InetAddress ia = InetAddress.getByName(hostAddress);
                    DatagramPacket sPacket,rPacket;
                    byte[] sbuffer, rbuffer = new byte[1024];
                    rPacket = new DatagramPacket(rbuffer, rbuffer.length, ia, udpPort);
                    udpSocket = new DatagramSocket();
                    sbuffer = netBuffer.getBytes();
                    System.out.println("Sending: " + netBuffer);
                    sPacket = new DatagramPacket(sbuffer, sbuffer.length, ia, udpPort);
                    udpSocket.send(sPacket);
                    udpSocket.receive(rPacket);
                    retstring = new String(rPacket.getData(), 0, rPacket.getLength());
                    System.out.println("Response: " + retstring);
                    udpSocket.close();
                    
                }catch(Exception e){ 
                	e.printStackTrace(); 
                }
            }
 
        } catch (Exception e) {
            e.printStackTrace();
        }
    	return retstring;
    }

}
