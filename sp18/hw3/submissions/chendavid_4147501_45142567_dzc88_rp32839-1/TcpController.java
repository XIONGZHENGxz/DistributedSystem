import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class TcpController implements Runnable {
	private BufferedReader reader;
	private Library library;
	private PrintWriter writer;
	private Socket socket;
	
	public TcpController(Socket _socket, Library _library) throws IOException {
		socket = _socket;
		library = _library;
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
	}

	@Override
	public void run() {
		try {
			String input;
			while ((input = reader.readLine()) != null) {
				String[] tokens = StringUtils.quoteSplit(input);
				System.out.println(Arrays.toString(tokens));
				String message = "";
		        if (tokens[0].equals("borrow")) {
		        	message = library.borrowBook(tokens[1], tokens[2]);
		        } else if (tokens[0].equals("return")) {
		        	message = library.returnRecordId(Integer.parseInt(tokens[1]));
		        } else if (tokens[0].equals("inventory")) {
		        	message = library.inventory();
		        } else if (tokens[0].equals("list")) {
		        	message = library.list(tokens[1]);
		        } else if (tokens[0].equals("exit")) {
		        	try {
		        		message = library.exit();
		        	} catch (IOException e) {
		        		e.printStackTrace();
		        	}
		        } else {
		        	message = "Invalid command.";
		        }
		        writer.print(message + "\r\n");
		        writer.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
