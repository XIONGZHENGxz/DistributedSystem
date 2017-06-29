import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;

import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

public class Client {
	public static int timeout=100;
	public static void main (String[] args) {
		Scanner sc=null;
		try{
			sc = new Scanner(new FileReader(args[0]));
		} catch(FileNotFoundException e){
			e.printStackTrace();
		}
		int numServer = sc.nextInt();
		System.out.println("number of server: "+numServer);
		List<String> servers=new ArrayList<>();
		List<String> ports=new ArrayList<>();
		for (int i = 0; i < numServer; i++) {
			String str=sc.next();
			System.out.println("server: "+str);
			String[] strs=str.split(":");
			servers.add(strs[0]);
			ports.add(strs[1]);
		}

		BufferedWriter out=null;
		BufferedReader in=null;
		sc.nextLine();
		while(sc.hasNextLine()) {
			String cmd=sc.nextLine();
			System.out.println("new request..."+cmd);
			Socket socket=new Socket();
			try{
				socket.connect(new InetSocketAddress(servers.get(0),Integer.parseInt(ports.get(0))),timeout);
				out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out.write(cmd);
				out.flush();
				String ret="";
				String line="";
//				while((line=in.readLine())!=null){
//					ret+=line;
//				}
//				System.out.println("return message..."+ret);
			} catch(SocketTimeoutException e){
				System.out.println(servers.get(0)+":"+ports.get(0)+" is dead!");
				servers.remove(0);
				ports.remove(0);
				continue;
			} catch(Exception e){
				e.printStackTrace();
			} finally{
				try{
					socket.close();
				} catch(IOException e){
					e.printStackTrace();
				}
			}
			System.out.println("done");
		}
	}
}
