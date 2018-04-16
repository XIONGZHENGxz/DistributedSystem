import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.NoSuchFileException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookServer {
	private final static Logger LOGGER = Logger.getLogger(BookServer.class.getName());
	private final static int TCP_PORT = 7000;
	private final static int UDP_PORT = 8000;

	public static void main(String[] args) {
		if (args.length < 1) {
			LOGGER.log(Level.SEVERE, "Provide initial inventory file as the first argument");
			System.exit(-1);
		}

		try {
			Library library = Library.fromFile(args[0]);

			new Thread(new Runnable() {

				@Override
				public void run() {
					try (DatagramSocket client = new DatagramSocket(UDP_PORT)) {
						LibraryClient lc = new LibraryClient(library);
						while (true) {
							byte[] buffer = new byte[1024];
							DatagramPacket data = new DatagramPacket(buffer, buffer.length);
							client.receive(data);
							String line = new String(data.getData()).trim();
							String out = lc.execute(line);
							Scanner tokens = new Scanner(out);
							while (tokens.hasNextLine()) {
								byte[] sbuffer = tokens.nextLine().getBytes(); 
								DatagramPacket send = new DatagramPacket(sbuffer, sbuffer.length,
										data.getAddress(), data.getPort());
								client.send(send);
							}
							tokens.close();
						}
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, "Server aborted: ", e);
					}

				}

			}).start();

			new Thread(new Runnable() {

				@Override
				public void run() {
					try (ServerSocket listener = new ServerSocket(TCP_PORT)) {
						while (true) {
							Socket client = listener.accept();
							new Thread(new Runnable() {
								@Override
								public void run() {
									LibraryClient lc = new LibraryClient(library);
									try (PrintStream out = new PrintStream(client.getOutputStream(), true);
											Scanner in = new Scanner(client.getInputStream())) {
										out.println(lc.execute(in.nextLine()));
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}).start();
						}
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, "Server aborted: ", e);
					}
				}
			}).start();

		} catch (NoSuchFileException e) {
			LOGGER.log(Level.SEVERE, "File not found: " + args[0]);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "IO Error", e);
		}

	}
}