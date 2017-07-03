package uta.shan.replicationBasedDS;

import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Map;
import java.util.HashMap;

import uta.shan.communication.Messager;
import uta.shan.communication.Util;
import uta.shan.config.ConfigReader;
import uta.shan.paxos2.*;


public class Server<K,V> {

	private Listener listener;
	private String[] servers;//servers in the same group
	private int[] ports;
	private int me;
	private int gid;//group id
	private int seed;
	private ReentrantLock lock;
	private Paxos<Operation<K,V>> paxos;
	private Map<K,V> store;

	public Server(int me,int gid, String[] servers,int[] paxosPorts, int port){
		this.gid = gid;
		this.seed=0;
		this.me=me;
		this.servers=servers;
		this.ports = paxosPorts;
		this.paxos=new Paxos(servers,ports,me);
		store=new HashMap<>();
		lock=new ReentrantLock();
		listener = new Listener(port,this);
		listener.start();
	}

	//get me
	public int getMe() {
		return this.me;
	}

	public Reply<V> handleRequest(Request<K,V> request) {
		if(request.getType().equals("get")) {
			return get(request);
		} else if(request.getType().equals("put")) {
			return put(request);
		} else if(request.getType().equals("remove")) {
			return remove(request);
		} else {
			return null;
		}
	}

	//get for testing
	public void sendStore(Socket socket) {
		catchup();
		Messager.sendMsg(store,socket);
	}

	//read value
	public Reply<V> get(Request<K,V> arg){
		K key=arg.getKey();
		agree(new Operation<K,V>(arg.getRid(),arg.getKey(),null,"get"));
		if(!store.containsKey(key)) return new Reply<V>(null,false);
		return new Reply<V>(store.get(key),true);
	}

	//remove key
	public Reply<V> remove(Request<K,V> arg) {
		K key = arg.getKey();
		V val = store.get(key);
		agree(new Operation<K, V>(arg.getRid(),arg.getKey(),null,"remove"));
		if(!store.containsKey(key)) return new Reply<V>(val,true);
		return new Reply<V>(null,false);
	}

	//commit operation
	public void commitOperation(int seq,Operation<K,V> op){
		if(op.getType().equals("put")){
			store.put(op.getKey(),op.getValue());
		}else if (op.getType().equals("remove")){
			store.remove(op.getKey());
		}
		paxos.getLearner().doneSeq(seq);
	}

	//catch up
	public void catchup() {
		int i = seed+1;
		while(true) {
			Status status = paxos.getLearner().getStatus(i);
			if(status == Status.PENDING) break;
			else commitOperation(i,(Operation<K,V>)paxos.getLearner().getSeqMap().get(i).getValue());
			i++;
		}
	}

	//reach agreement
	public Object reachAgreement(int seq) {
		while(true) {
			Instance inst = paxos.getLearner().getSeqMap().get(seq);
			if(inst != null && inst.getStatus() == Status.DECIDED){
				return inst.getProposal().getValue();
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


	//get seq
	public Operation<K,V> getSeq(int seq) {
		Map<Integer,Instance<Operation<K,V>>> map = paxos.getLearner().getSeqMap();
		Instance<Operation<K,V>> inst = map.get(seq);
		if(!map.containsKey(seq)) return null;
		return (Operation<K, V>) inst.getValue();
	}

	//agree on an operation
	public void agree(Operation<K,V> op){
		while(true) {
			seed++;
			Status status = paxos.getLearner().getStatus(seed);
			if (status == Status.PENDING) {
				paxos.startConcensus(seed, op);
				Operation<K,V> agreed = (Operation<K,V>) reachAgreement(seed);
				if(Util.DEBUG) System.out.println(me+" complete concensus..."+ seed + " "+agreed.getType());
				commitOperation(seed, agreed);
				if(agreed.getRid().equals(op.getRid())) break;
			} else if (status == Status.DECIDED)
				commitOperation(seed, (Operation<K,V>) paxos.getLearner().getSeqMap().get(seed).getValue());
		}
	}

	//write value
	public Reply<V> put(Request<K,V> arg){
		lock.lock();
		K key=arg.getKey();
		V val=arg.getValue();
		agree(new Operation<K,V>(arg.getRid(), arg.getKey(),arg.getValue(),"put"));
		lock.unlock();
		return new Reply(null,true);
	}


	public static void main(String...args) {
		int gid = Integer.parseInt(args[0]);
		int id = Integer.parseInt(args[1]);
		int[] nums = new int[2];
		ConfigReader.readNumGroups(args[2],nums);
		String[][] servers = new String[nums[0]][nums[1]];
		int[][] ports = new int[nums[0]][nums[1]];
		ConfigReader.readServers(args[2],servers,ports);
		Server<Integer,Integer> server = new Server<>(id,gid,servers[gid],ports[gid], Util.clientPort);
	}
}

