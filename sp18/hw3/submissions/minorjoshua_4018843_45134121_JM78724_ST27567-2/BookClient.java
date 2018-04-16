import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

public class BookClient {
    static String[] validCommands = {"borrow", "return", "list", "inventory"};

	public static void main (String[] args) {
		String hostAddress;
		String mode = "U";
		int tcpPort;
		int myUdpPort;
		int baseUdpPort;
		int clientId;

		if (args.length != 2) {
			System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
			System.out.println("\t(1) <command-file>: file with commands to the server");
			System.out.println("\t(2) client id: an integer between 1..9");
			System.exit(-1);
		}

		String commandFile = args[0];
		clientId = Integer.parseInt(args[1]);
		String fileName = "out_" + clientId + ".txt";
		hostAddress = "localhost";

		tcpPort = 7000;// hardcoded -- must match the server's tcp port
		baseUdpPort = 8000;// hardcoded -- must match the server's udp port

		try {
			Scanner sc = new Scanner(new FileReader(commandFile));
            InetAddress addr = InetAddress.getByName(hostAddress);
            DatagramSocket udp = new DatagramSocket();
            byte[] udpClientId = (String.valueOf(clientId)).getBytes();
            DatagramPacket udpHandshakePacket = new DatagramPacket(udpClientId, udpClientId.length, addr, baseUdpPort);
            udp.send(udpHandshakePacket);
            byte[] receiveID = new byte[1024];
            DatagramPacket idPacket = new DatagramPacket(receiveID, receiveID.length, addr, baseUdpPort);
            udp.receive(idPacket);
            myUdpPort = Integer.parseInt(new String(receiveID, 0, idPacket.getLength()));

            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileName, true));

            while(sc.hasNextLine()) {
                String cmd = sc.nextLine();
//                System.out.println("Reading next line: " + cmd);
                String[] tokens = cmd.split(" ");
                boolean shouldSend = true;
				if (tokens[0].equals("setmode")) {
				    shouldSend = false;
				    mode = tokens[1]; //Assign mode to cmd parameter.  No need to communicate with server for this command
				} else if (tokens[0].equals("exit")) {
                    fileWriter.close();
					System.exit(0);
				} else {
				    if(!Arrays.asList(validCommands).contains(tokens[0])) {
                        shouldSend = false;
                        System.out.println("ERROR: No such command");
                    }
				}
				if(shouldSend) {
//                    System.out.println("Should send: " + cmd + "\t" + mode);
                    if(mode.equals("T")) { //TCP
                        Socket tcp = new Socket(addr, tcpPort);

                        PrintWriter out = new PrintWriter(tcp.getOutputStream(),true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(tcp.getInputStream()));

                        out.println(cmd);
                        String response = in.readLine();
                        if(response.contains("___n___")) {
                            response = String.join("\n", response.split("___n___"));
                        }
                        System.out.println("TCP Response: " + response);
                        fileWriter.append(response).append(String.valueOf('\n'));

                        out.close();
                        in.close();
                        tcp.close();
                    } else { //UDP
                        byte[] b = cmd.getBytes();
                        byte[] receive = new byte[1024];

                        DatagramPacket packet = new DatagramPacket(b, b.length, addr, myUdpPort);
                        udp.send(packet);

                        DatagramPacket responsePacket = new DatagramPacket(receive, receive.length, addr, myUdpPort);
                        udp.receive(responsePacket);
                        String response = new String(receive, 0, responsePacket.getLength());
                        System.out.println("UDP Response: " + response);
                        fileWriter.append(response).append(String.valueOf('\n'));
                    }
                }
            }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
