package uta.shan.messager;

import uta.shan.paxos2.Listener;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;

public class Messager {

	private static final int port = 88888;

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

	public static Object getMsg(Socket socket) {
		Object resp = null;
		ObjectInputStream inputStream;
		try {
			inputStream = new ObjectInputStream(socket.getInputStream());
			resp = inputStream.readObject();
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
		return resp;
	}
}
