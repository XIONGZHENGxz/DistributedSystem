import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
public class TestServer{
	private Client client;
	private String ipAddr;
	@Before
	public void createClient(){
		try{
		InetAddress ip=InetAddress.getLocalHost();
		ipAddr=ip.getHostAddress();
		client=new Client(ipAddr,"192.168.245.149",1099);
		}catch(UnknownHostException e){
			e.printStackTrace();
		}
	}

	@Test
	public void test1(){
		assertTrue(client.host.equals(ipAddr));
		assertTrue(client.server.equals("192.168.245.149"));
		assertTrue(client.serverPort==1099);
	}

	@Test 
	public void test2(){
		client.put("zheng","xiong");
		GetReply gr=client.Get("zheng");
		assertEquals("xiong",gr.value);
	}

	@Test
	public void test3(){
		client.Append("a","b");
		GetReply gr=client.Get("a");
		assertEquals("b",gr.value);
	}

}


	
