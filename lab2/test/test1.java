import java.net.InetAddress;
import java.net.UnknownHostException;
public class test1{
	public static void main(String...args){
		try{
		InetAddress ip=InetAddress.getLocalHost();
		System.out.println(ip.getHostAddress());
		}catch(UnknownHostException e){
			e.printStackTrace();
		}
	}
}
	
