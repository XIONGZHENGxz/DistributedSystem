import org.junit.Test;
import static org.junit.Assert.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import uta.shan.ds.server.Server;
import uta.shan.ds.client.Client;
import java.util.concurrent.TimeUnit;

public class TestServer{
	String[] servers;
	int[] ports;
	ExecutorService es=Executors.newFixedThreadPool(10);
	private final static String host="192.168.245.150";
	public void initTest(int n){
		servers=new String[n];
		ports=new int[n];
		for(int i=0;i<n;i++){
			servers[i]=host;
			ports[i]=i+1099;
		}
	}

	//test constructor
	@Test
	public void test1(){
		int n=2;
		this.initTest(n);
		assertTrue(servers[0]==host);
		for(int i=0;i<n;i++){
			Server s=new Server(i,servers,ports);
			assertTrue(s.me==i);
			es.execute(s);
		}
		es.shutdown();
		Client client=new Client(host,servers,ports);
		assertTrue(client.me.equals(host));
		assertTrue(client.servers.length==n);
		client.put("google","amazon");
		assertTrue(client.get("google").equals("amazon"));
	}
}
