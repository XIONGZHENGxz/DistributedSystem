package uta.shan.communication;

import java.io.*;

import java.net.Socket;
import java.net.InetSocketAddress;

public class Messager {

	//send msg
	public  static void sendMsg(Object msg, String host, int port) {
		ObjectOutputStream out;
		Socket socket = new Socket();
		try {
			InetSocketAddress addr = new InetSocketAddress(host,port);
			socket.connect(addr);
			out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(msg);
			out.flush();
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			if(socket!=null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	//send and wait for response
	public static String sendAndWaitReply(String msg,String host,int port) {
		try {
			InetSocketAddress addr = new InetSocketAddress(host,port);
			Socket socket = new Socket();
			socket.connect(addr, Util.TIMEOUT);
			PrintStream ps = new PrintStream(socket.getOutputStream());
			ps.println(msg);
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String resp = null;
			long startTime = System.currentTimeMillis();
			long currTime = System.currentTimeMillis();
			while(resp == null && currTime-startTime < Util.TIMEOUT) {
				resp = br.readLine();
				currTime = System.currentTimeMillis();
			}
			return resp;
		} catch (IOException e) {
			return null;
		}
	}

	public static Object sendAndWait(String msg, String host, int port) {
		try {
			InetSocketAddress addr = new InetSocketAddress(host,port);
			Socket socket = new Socket();
			socket.connect(addr, Util.TIMEOUT);
			PrintStream ps = new PrintStream(socket.getOutputStream());
			ps.println(msg);
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			Object resp = null;
			long startTime = Util.getCurrTime();
			long currTime = Util.getCurrTime();
			while(resp == null && currTime-startTime < Util.TIMEOUT) {
				resp = in.readObject();
				currTime = Util.getCurrTime();
				if(Util.DEBUG) {
					System.out.println("Debug: waiting for reply from "+host+" "+port);
				}
			}
			return resp;
		} catch (IOException e) {
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object sendAndWaitReply(Object msg,String host, int port) {
		try {
			InetSocketAddress addr = new InetSocketAddress(host,port);
			Socket socket = new Socket();
			socket.connect(addr, Util.TIMEOUT);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(msg);
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			Object resp = null;
			long startTime = Util.getCurrTime();
			long currTime = Util.getCurrTime();
			while(resp == null && currTime-startTime < Util.TIMEOUT) {
				resp = in.readObject();
				currTime = Util.getCurrTime();
				if(Util.DEBUG) {
					System.out.println("Debug: waiting for reply from "+host+" "+port);
				}
			}
			return resp;
		} catch (IOException e) {
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public  static void sendMsg(Object msg, Socket socket) {
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(msg);
			out.flush();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	//if flag == 0, read object, else read string
	public static Object getMsg(Socket socket) {
		Object resp = null;
		ObjectInputStream inputStream;
		try {
			inputStream = new ObjectInputStream(socket.getInputStream());
			resp = inputStream.readObject();
		} catch(Exception e){
			e.printStackTrace();
		}
		return resp;
	}

	public static void sendMsg(String msg, String host, int port) {
		try {
			Socket socket = new Socket();
			InetSocketAddress addr = new InetSocketAddress(host,port);
			socket.connect(addr);
			PrintStream ps = new PrintStream(socket.getOutputStream());
			ps.println(msg);
		} catch (IOException e) {
			e.printStackTrace();
//            System.out.println("remote server "+host+" "+port+" is down!");
		}
	}

	public static void sendMsg(String msg, Socket socket) {
		try {
			PrintStream ps = new PrintStream(socket.getOutputStream());
			ps.println(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getRequest(Socket socket) {
		String resp = "";
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			resp = reader.readLine();
			if(Util.DEBUG) {
				System.out.println("received request: " + resp);
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		return resp;
	}
}
