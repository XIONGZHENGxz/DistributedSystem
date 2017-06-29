import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Comparator;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import mutex.Timestamp;
import clocks.LamportClock;

public class Server {
	public static int timeout=100;
	LamportClock lc;
	int orderId;
	int myID;
	Queue<Request> q;//request queue
	Map<String,Integer> store;
	Map<String,Set<Integer>> users;//user->set of orderID
	Map<Integer,Order> orders;//orderID->Order
	BufferedWriter out;
	BufferedReader in;
	ServerSocket serverSocket;
	int numAcks;
	int n;//number of server
	List<String> servers;
	List<Integer> ports;
	public Server(int numServer,int myID,Map<String,Integer> store,List<String> servers,List<Integer> ports){
		n=numServer;
		orderId=1;
		this.myID=myID;
		q=new PriorityQueue<>(new Comparator<Request>(){
				public int compare(Request a,Request b){
				return b.compare(a);
				}
				});
		users=new HashMap<>();
		orders=new HashMap<>();
		lc=new LamportClock();
		this.servers=servers;
		this.ports=ports;
		this.store=store;
	}

	public static void main (String[] args) {
		Scanner sc=null;
		try{
			sc = new Scanner(new File(args[0]));
		} catch(FileNotFoundException e){
			e.printStackTrace(); 
		}
		int myID = sc.nextInt();
		int numServer = sc.nextInt();
		String inventoryPath = sc.next();
		Map<String,Integer> store=new HashMap<>();
		List<String> servers=new ArrayList<>();
		List<Integer> ports=new ArrayList<>();
		System.out.println("[DEBUG] my id: " + myID);
		System.out.println("[DEBUG] numServer: " + numServer);
		System.out.println("[DEBUG] inventory path: " + inventoryPath);
		for (int i = 0; i < numServer; i++) {
			// TODO: parse inputs to get the ips and ports of servers
			String str = sc.next();
			System.out.println("address for server " + i + ": " + str);
			String[] strs=str.split(":");
			servers.add(strs[0]);
			ports.add(Integer.parseInt(strs[1]));
		}
		//parse inventory file 
		try{
			File inventory=new File(inventoryPath);
			BufferedReader br=new BufferedReader(new FileReader(inventory));
			String item="";
			while((item=br.readLine())!=null){
				String[] items=item.split(" ");
				store.put(items[0],Integer.parseInt(items[1]));
			}
			br.close();
		} catch(IOException e){
			System.err.println("file not found");
		}
		Server myServer=new Server(numServer,myID,store,servers,ports);
		//start server socket to communicate with clients and other servers
		myServer.start();
	}

	public void start(){
		try{
			serverSocket=new ServerSocket(ports.get(myID-1));
		} catch(IOException e){
			e.printStackTrace();
		}
		while (true) {
			try{
				Socket socket=serverSocket.accept();
				System.out.println("socket connected...");
				SocketHandlerThread thread=new SocketHandlerThread(socket,this);
				thread.start();
			} catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	public synchronized void handler(String req,InetSocketAddress addr){
		String[] reqs=req.split(" ");
		if(reqs[0].equals("purchase")){
			Order order=new Order(reqs[1],reqs[2],Integer.parseInt(reqs[3]),orderId++);
			sendMsg(purchase(order),addr);
		}else if(reqs[0].equals("cancel")){
			sendMsg(cancel(Integer.parseInt(reqs[1])),addr);
		}else if(reqs[0].equals("search")){
			sendMsg(search(reqs[1]),addr);
		}else if(reqs[0].equals("list")){
			sendMsg(list(),addr);
		}
		else System.out.println("invalid operation from client....");
	}

	public synchronized void requestCS(){
		lc.tick();
		for(int i=0;i<servers.size();i++){
			if(myID==i+1) continue;
			sendMsg("request,"+myID,new InetSocketAddress(servers.get(i),ports.get(i)));
		}
		numAcks=0;
		while(q.peek().getPid()!=myID || numAcks<n-1);
		System.out.println("acks..."+numAcks);
	}

	public synchronized void releaseCS(){
		lc.tick();
		q.poll();
		for(int i=1;i<servers.size();i++){
			sendMsg("release",new InetSocketAddress(servers.get(i),ports.get(i)));
		}
	}

	public void sendMsg(String message,InetSocketAddress addr){
		System.out.println("send message..."+message);
		Socket socket=new Socket();
		try{
			socket.connect(addr);
			out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out.write(message);
			out.flush();
		} catch (IOException e){
			e.printStackTrace();
		} finally{
			try{
				socket.close();
			} catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	public void release(String req){
		q.poll();
		String[] reqs=req.split(" ");
		if(reqs[1].equals("purchase")){
			Order order=new Order(reqs[1],reqs[2],Integer.parseInt(reqs[3]),orderId++);
			purchase(order);
		}else if(reqs[1].equals("cancel")){
			cancel(Integer.parseInt(reqs[2]));
		}
	}

	public String purchase(Order order){
		if(!store.containsKey(order.product)) return "Not available - we do not sell this product";
		if(order.quantity>store.get(order.product)) return "Not Available - Not enough items";
		store.put(order.product,store.get(order.product)-order.quantity);
		orders.put(order.orderId,order);
		if(!users.containsKey(order.user)) users.put(order.user,new HashSet<>());
		users.get(order.user).add(order.orderId);
		return "Your order has been placed "+order.orderId+" "+order.user+" "+order.product+" "+String.valueOf(order.quantity);
	}

	public String cancel(int orderId){
		if(!orders.containsKey(orderId)) return String.valueOf(orderId)+" not found, no such order";
		Order order=orders.get(orderId);
		orders.remove(orderId);
		users.get(order.user).remove(orderId);
		if(users.get(order.user).size()==0) users.remove(order.user);
		return "Order "+String.valueOf(orderId)+" is canceled";
	}

	public String search(String name){
		StringBuilder res=new StringBuilder();
		if(!users.containsKey(name)) return "No order found for "+name;
		for(int orderId:users.get(name)){
			res.append(orderId);
			Order order=orders.get(orderId);
			res.append(order.product);
			res.append(order.quantity);
			res.append("\n");
		}
		return res.toString();
	}

	public String list(){
		StringBuilder res=new StringBuilder();
		for(String product:store.keySet()){
			res.append(product+" "+store.get(product));
			res.append("\n");
		}
		return res.toString();
	}
}


class Order{
	String user;
	String product;
	int quantity;
	int orderId;
	public Order(String u,String p,int q,int id){
		user=u;
		product=p;
		quantity=q;
		orderId=id;
	}
}
