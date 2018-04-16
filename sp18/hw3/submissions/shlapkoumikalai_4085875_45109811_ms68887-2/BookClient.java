import java.io.*;
import java.util.*;
import java.net.*;

public class BookClient {
	private static final String HOST_ADDRESS ="localhost";
	private static final int SERVER_PORT = 2018;

	private static String sendData(String command, String communicationMode, String hostAddress, int port) {
		if (communicationMode.equals("U")) {
			//System.out.println("Sending UDP");
			return udpSendReceive(command, hostAddress, port);
		} else {
			//System.out.println("Sending TCP");
			return tcpSendReceive(command, hostAddress, port);
		}
	}

	private static String udpSendReceive(String command, String hostAddress, int port) {
		int len = 1024;
		byte[] rbuffer = new byte[len];
		DatagramPacket sPacket, rPacket;
		try {
			InetAddress ia = InetAddress.getByName(hostAddress);
			DatagramSocket datasocket = new DatagramSocket();
			byte[] buffer = command.getBytes();
			sPacket = new DatagramPacket(buffer, buffer.length, ia, port);
			datasocket.send(sPacket);
			rPacket = new DatagramPacket(rbuffer, rbuffer.length);
			datasocket.receive(rPacket);
			String retstring = new String(rPacket.getData(), 0, rPacket.getLength());
			datasocket.close();
			return retstring;
		} catch (UnknownHostException e) {
			System.err.println(e);
		} catch (SocketException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}
		return null;
	}

	private static void udpExit(String command, String hostAddress, int port) {
		DatagramPacket sPacket;
		try {
			InetAddress ia = InetAddress.getByName(hostAddress);
			DatagramSocket datasocket = new DatagramSocket();
			byte[] buffer = command.getBytes();
			sPacket = new DatagramPacket(buffer, buffer.length, ia, port);
			datasocket.send(sPacket);
			datasocket.close();
		} catch (UnknownHostException e) {
			System.err.println(e);
		} catch (SocketException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	public static String tcpSendReceive(String command, String hostAddress, int port) {
		try {
			Socket clientSocket = new Socket(hostAddress, port);
			Scanner sc = new Scanner(clientSocket.getInputStream());
			PrintWriter pout = new PrintWriter(clientSocket.getOutputStream());
			pout.println(command);
			pout.flush();
			String retstring = sc.nextLine();
			sc.close();
			clientSocket.close();
			return retstring;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void tcpExit(String command, String hostAddress, int port) {
		try {
			Socket clientSocket = new Socket(hostAddress, port);
			PrintWriter pout = new PrintWriter(clientSocket.getOutputStream());
			pout.println(command);
			pout.flush();
			clientSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
			System.out.println("\t(1) <command-file>: file with commands to the server");
			System.out.println("\t(2) client id: an integer between 1..9");
			System.exit(-1);
		}

		String commandFile = args[0];
		int clientId = Integer.parseInt(args[1]);
		int port = SERVER_PORT;

		String communicationMode = null;
		try {
			Scanner sc = new Scanner(new FileReader(commandFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter("out_" + clientId + ".txt"));

			while (sc.hasNextLine()) {
				String cmd = sc.nextLine();
				String[] tokens = cmd.split(" ");

				switch (tokens[0])
				{
					case "setmode":
						//if first time, send through UDP by default
						if(communicationMode == null)
						{
							communicationMode = tokens[1];
							String portString = sendData(communicationMode, "U", HOST_ADDRESS, SERVER_PORT);
							port = Integer.parseInt(portString);
							//System.out.println(port);
						}
						//if not first time, then we will be using the worker thread setmode
						else
						{
							String newCommunicationMode = tokens[1];
							//no point in making a new thread if same mode, use old
							if(!communicationMode.equals(newCommunicationMode))
							{
								String portString = sendData(newCommunicationMode, communicationMode, HOST_ADDRESS, port);
								communicationMode = newCommunicationMode;
								port = Integer.parseInt(portString);
								//System.out.println(port);
							}
						}
						break;
					case "borrow":
					case "return":
					{
						if (communicationMode == null)
						{
							communicationMode = "U";
							String portString = sendData(communicationMode, "U", HOST_ADDRESS, SERVER_PORT);
							port = Integer.parseInt(portString);
						}

						String returnString = sendData(cmd, communicationMode, HOST_ADDRESS, port);
						//System.out.println(returnString);
						writer.write(returnString + "\n");
						//writer.newLine();
						break;
					}
					case "inventory":
					case "list":
					{
						if (communicationMode == null)
						{
							communicationMode = "U";
							String portString = sendData(communicationMode, "U", HOST_ADDRESS, SERVER_PORT);
							port = Integer.parseInt(portString);
						}

						String returnString = sendData(cmd, communicationMode, HOST_ADDRESS, port);
						//System.out.println(returnString);
						String[] returnStringList = returnString.split(",");
						for (String output : returnStringList)
						{
							writer.write(output + "\n");
							//writer.newLine();
						}
						break;
					}
					case "exit":
					{
						if (communicationMode == null)
						{
							communicationMode = "U";
							String portString = sendData(communicationMode, "U", HOST_ADDRESS, SERVER_PORT);
							port = Integer.parseInt(portString);
						}

						if (communicationMode.equals("U"))
						{
							udpExit(cmd, HOST_ADDRESS, port);
						}
						else
						{
							tcpExit(cmd, HOST_ADDRESS, port);
						}
						break;
					}
					default:
						System.out.println("ERROR: No such command");
						break;
				}
			}

			sc.close();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("DONE");
	}
}
