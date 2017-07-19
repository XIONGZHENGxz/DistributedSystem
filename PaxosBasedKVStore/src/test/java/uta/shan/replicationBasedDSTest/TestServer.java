package uta.shan.replicationBasedDSTest;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import uta.shan.common.Comparator;
import uta.shan.communication.Util;
import uta.shan.config.ConfigReader;
import uta.shan.replicationBasedDS.Server;
import uta.shan.replicationBasedDS.Client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.StringTokenizer;

public class TestServer{
	private static String[][] hosts;
	private static int[][] ports;
	private static int[][] paxosPorts;
	private static Server<Integer,String>[][] servers;

	private static String inputFile = "input.json";
	private static final String LOCALHOST="localhost";
	private static final int NUM_GROUPS = 2;
	private static final int NUM_SERVERS_PER_GROUP = 3;

	@BeforeClass
	public static void init(){
		hosts = new String[NUM_GROUPS][NUM_SERVERS_PER_GROUP];
		paxosPorts = new int[NUM_GROUPS][NUM_SERVERS_PER_GROUP];
		ports = new int[NUM_GROUPS][NUM_SERVERS_PER_GROUP];
		servers = new Server[NUM_GROUPS][NUM_SERVERS_PER_GROUP];
		for(int i =0;i<NUM_GROUPS;i++) {
			for(int j=0;j<NUM_SERVERS_PER_GROUP;j++) {
				hosts[i][j] = LOCALHOST;
				paxosPorts[i][j] = Util.paxosPort+i*NUM_SERVERS_PER_GROUP+j;
				ports[i][j] = Util.clientPort+i*NUM_SERVERS_PER_GROUP+j;
				servers[i][j] = new Server<>(j,i,hosts[i],paxosPorts[i],ports[i][j]);
			}
		}
	}

	//test constructor
	@Test
	public void test1(){
		Client<Integer,String> client = new Client<>(LOCALHOST,hosts,ports);
		assertTrue(client.getMe().equals(LOCALHOST));
		assertTrue(client.getServers().length == NUM_GROUPS);
		client.put(1,"amazon");
		assertTrue(client.get(1).equals("amazon"));
		assertTrue(servers[1][0].getSeq(1).getType().equals("put"));
		assertTrue(servers[1][1].getSeq(1).getKey() == 1);
		assertTrue(servers[1][0].getSeq(2).getType().equals("get"));
		assertTrue(servers[1][1].getSeq(2).getKey() == 1);
	}

	@Test
	public void test2(){
		Client<Integer,String> client = new Client<>(LOCALHOST,hosts,ports);
		client.put(2,"Google");
		assertTrue(client.get(2).equals("Google"));
		assertTrue(client.remove(2));
		assertTrue(client.get(2) == null);
	}

	@Test
	public void test3() {
		Client<Integer,Integer> client = new Client<>(LOCALHOST,hosts,ports);
		Map<Integer,Integer> store = new HashMap<>();
		List<String> ops = new ArrayList<>();
		ConfigReader.readOperations(inputFile,ops);
		for(int i=0;i<ops.size()-1;i++) {
			StringTokenizer st = new StringTokenizer(ops.get(i));
			String arg = st.nextToken();
			int key = -1;
			int val = -1;
			if (arg.equals("get") || arg.equals("remove")) key = Integer.parseInt(st.nextToken());
			else if (arg.equals("put")) {
				key = Integer.parseInt(st.nextToken());
				val = Integer.parseInt(st.nextToken());
			}
			client.doOperation(arg, key, val, store);
		}
		assertTrue(client.checkConsistency(new Comparator<Integer, Integer>()));
	}
}
