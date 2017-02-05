import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.net.UnknownHostException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.net.InetAddress;
public class Testpb{
	Client client;
	String ip;
	String server1;//primary server
	String server2;//backup server
	int port;
	ServerBase stub;
	@Before
	public void initClient(){
		server1="192.168.245.149";
		int port=1099;
		ip="192.168.245.156";
		client=new Client(ip,server1,port);
	}

	@Test
	public void test1(){
		try{	
			assertTrue(client.Ping(server1,port)==true)
			client.shutdown(server1,port);
			assertTrue(client.Ping(server1,port)==false);
			assertTrue(client.Ping(server2,port)==true);
			assertTrue(client.primary.equals(server2));
			client.resume(server1,port);
			assertTrue(client.primary.equals(server2));
		} catch(Exception e){
			e.printStackTrace();
		}
	}


	@Test
	public void test2(){
		try{
			client.shutdown(server1,port);
			client.shutdown(server2,port);
			assertTrue(client.Ping(server1,port)==false);
			assertTrue(client.Ping(server2,port)==false);
			client.resume(server1,port);
			client.resume(server2,port);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}



