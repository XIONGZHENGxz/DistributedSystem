package uta.shan.replicationBasedDS;
import org.omg.CORBA.TIMEOUT;
import uta.shan.communication.Messager;
import uta.shan.communication.Util;

import java.util.Random;

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
}




