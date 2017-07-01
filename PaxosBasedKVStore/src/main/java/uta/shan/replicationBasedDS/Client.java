package uta.shan.replicationBasedDS;
import uta.shan.communication.Messager;
import uta.shan.communication.Util;
import uta.shan.config.ConfigReader;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Client<K,V>{
	private String[][] servers;//one row is one group of servers
	private int[][] ports;
	private Random rand;
	private String me;

	public Client(String name,String[][] servers,int[][] ports){
		this.me=name;
		this.servers=servers;
		this.ports=ports;
		rand = new Random();
	}

	//get me
	public String getMe() {
		return this.me;
	}

	//get servers
	public String[][] getServers() {
		return this.servers;
	}

	//generate unique id for every request
	public String makeRequestId(){
		int r = rand.nextInt(10000000);
		return me+String.valueOf(r);
	}

	//get
	public V get(K key){
		int gid = decideGroup(key);
		Request<K,V> request = new Request<>(key,null,makeRequestId(),"get");
		Reply reply = null;
		int i=0;
		for(;i<servers[gid].length;i++){
			reply =(Reply) Messager.sendAndWaitReply(request,servers[gid][i],ports[gid][i]);
			if(reply!=null && reply.getStatus() == true) break;//if gr==null, then the server is down
		}

		if(i == servers[gid].length) return null;
		return (V)reply.getValue();
	}

	public boolean put(K key,V value){
		int gid = decideGroup(key);
		if(Util.DEBUG) {
			System.out.println("Debug: \ngid: "+gid);
		}
		Request<K,V> request = new Request<>(key,value,makeRequestId(),"put");
		Reply reply;
		for(int i = 0;i<servers[gid].length;i++) {
			reply = (Reply) Messager.sendAndWaitReply(request,servers[gid][i],ports[gid][i]);
			if(reply != null && reply.getStatus()) return true;
		}
		return false;
	}

	//remove
	public boolean remove(K key) {
		int gid = decideGroup(key);
		Request<K,V>  request = new Request<>(key,null,makeRequestId(),"remove");
		Reply<V> reply;
		for(int i=0;i<servers[gid].length;i++) {
			reply = (Reply<V>) Messager.sendAndWaitReply(request,servers[gid][i],ports[gid][i]);
			if(reply !=null && reply.getStatus()) return true;
		}
		return false;
	}

	public V doOperation(String arg, K key, V value, Map<K,V> store) {
		if(arg.equals("put")) {
			put(key,value);
			store.put(key,value);
		} else if(arg.equals("get")) {
			 return get(key);
		} else if(arg.equals("remove")) {
		    remove(key);
			store.remove(key);
		}
		return null;
	}

	//decide which group to get service
	public int decideGroup(K key) {
		return (int)getHashCode(key)%servers.length;
	}

	//a perfect hash function to divide keys
	public long getHashCode(K key) {
		if(key instanceof Integer) {
			return (Integer) key;
		} else{
			return key.hashCode();
		}
	}


	public static void main(String...args) {
		int[] nums = new int[2];
		ConfigReader.readNumGroups(args[0],nums);
		String me = "";
		try {
			me = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		Map<Integer,Integer> store = new HashMap<>();
		String[][] servers = new String[nums[0]][nums[1]];
		int[][] ports = new int[nums[0]][nums[1]];
		ConfigReader.readServers(args[0],servers,ports);
		int[][] serverPorts = new int[nums[0]][nums[1]];
		Arrays.fill(serverPorts,Util.clientPort);
		Client<Integer,Integer> client = new Client<>(me,servers,serverPorts);
		List<String> ops = new ArrayList<>();
		ConfigReader.readOperations(args[1],ops);
		for(String op: ops) {
			StringTokenizer st = new StringTokenizer(op);
			String arg = st.nextToken();
			int key = -1;
			int val = -1;
			if(arg.equals("get") || arg.equals("remove")) key = Integer.parseInt(st.nextToken());
			else if(arg.equals("put")) {
				key = Integer.parseInt(st.nextToken());
				val = Integer.parseInt(st.nextToken());
			}
			client.doOperation(arg,key,val,store);
		}
		for(int key: store.keySet()) {
			if(client.get(key) == store.get(key)) {
				System.out.println("match");
			} else {
				try {
					throw new Exception("key "+key+" not match");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}




