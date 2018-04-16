import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Scanner;

public class UDPServerThread extends Thread {
	DatagramSocket datasocket;
	byte[] buf_reply;
	Library library;

	public UDPServerThread(DatagramSocket ds, Library library) {
		this.datasocket = ds;
		this.library = library;
		buf_reply = new byte[1000];
	}

	private String getMessage(DatagramPacket datapacket) throws IOException {
		String response = new String(datapacket.getData(), 0, datapacket.getLength());
		System.out.println(response);
		response = response.split("\0")[0];
		return response;
	}

	private void sendMessage(String str, DatagramPacket datapacket) throws IOException {
		byte[] buf_data = new byte[1000];
		buf_data = str.getBytes();
		DatagramPacket replypacket = new DatagramPacket(buf_data, buf_data.length, datapacket.getAddress(),
				datapacket.getPort());
		datasocket.send(replypacket);
	}

	public void run() {

		String command;
		String tag;
		try {
			while (true) {
				DatagramPacket datapacket = new DatagramPacket(buf_reply, buf_reply.length);
				datasocket.receive(datapacket);
				command = getMessage(datapacket);
				System.out.println("Received " + command);
				Scanner st = new Scanner(command);
				tag = st.next();
				if (tag.equals("borrow")) {
					String student = st.next();
					st.skip(" ");
					String book = st.nextLine();
					int retValue = library.borrow(student, book);

					sendMessage(Integer.toString(retValue), datapacket);

				} else if (tag.equals("return")) {
					int id = st.nextInt();
					int retValue = library.ret(id);

					sendMessage(Integer.toString(retValue), datapacket);

				} else if (tag.equals("list")) {
					String student = st.next();
					String retValue = library.list(student);
					//System.out.println(retValue);

					sendMessage(retValue, datapacket);

				} else if (tag.equals("inventory")) {
					String retValue = library.listInventory();

					sendMessage(retValue, datapacket);

				} else if (tag.equals("exit")) {
					String retValue = library.listInventory();
					sendMessage(retValue, datapacket);
					datasocket.close();
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}