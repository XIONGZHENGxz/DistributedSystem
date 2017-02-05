import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.net.UnknownHostException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.net.InetAddress;
public class TestReadAndWrite{
	Client client;
	String ip;
	String server;
	int port;
	ServerBase stub;
	@Before
	public void initClient(){
		server="192.168.245.149";
		int port=1099;
		ip="192.168.245.156";
		client=new Client(ip,server,port);
	}

	@Test
	public void test1(){
		try{
			client.put("zheng","xiong");
			GetReply gr=client.get("zheng");
			assertTrue(gr.value.equals("xiong"));
			client.append("zheng","tym");
			gr=client.get("zheng");
			assertTrue(gr.value.equals("xiongtym"));
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	@Test
	public void test2(){
		try{
			client.shutdown(server1,port);
			GetReply gr=client.get("zheng");
			assertNotNull(gr);
			assertTrue(gr.value.equals("xiongtym"));
			client.put("tang","yanmei");
			gr=client.get("tang");
			assertTrue(gr.value.equals("yanmei"));
			client.resume(server1,port);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Test
	public void test3(){
		try{
			client.shutdown(server2,port);
			GetReply gr=client.get("zheng");
			assertNotNull(gr);
			assertTrue(gr.value.equals("xiongtym"));
			client.put("sun","xun");
			gr=client.get("sun");
			assertTrue(gr.value.equals("xun"));
			gr=client.append("sun","xun");
			assertTrue(gr.value.equals("xunxun"));
		} catch(Exception e){
			e.printStackTrace();
		}
	}

}



