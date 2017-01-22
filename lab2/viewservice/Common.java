import java.time.Duration;
public class Common{
	public static long PingInterval=Duration.ofMillis(100).toMillis();//ping frequency
	public static int DeadPings=5;//the viewserver declare client is dead if it misses this many pings.
}
class GetArgs{
}

class GetReply{
	View view;
}
	
