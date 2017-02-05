import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.net.UnknownHostException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.net.InetAddress;
public class TestResume{
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
	public void test2(){
		try{
			client.resume();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

}



