import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.net.UnknownHostException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.net.InetAddress;
public class TestShutdownAndResume{
	Client client;
	String ip;
	String server;
	int port;
	ServerBase stub;
	@Before
	public void initClient(){
		server="192.168.245.149";
		int port=1099;
		try{
			InetAddress ia=InetAddress.getLocalHost();
			ip=ia.getHostAddress();
			client=new Client(ip,server,port);
			Registry registry = LocateRegistry.getRegistry(server,port);
			ServerBase stub=(ServerBase) registry.lookup("key/value store");
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	@Test
	public void test1(){
		try{
			stub.Shutdown();
		} catch(Exception e){
			e.printStackTrace();
		}
	//	stub.Ping();
	}
}



