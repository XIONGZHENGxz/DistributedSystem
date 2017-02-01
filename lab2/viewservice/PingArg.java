import java.io.Serializable;
public class PingArg implements Serializable{
	static final long serialVersionUID = 42L;
	int viewNum;
	String hostPort;
	public PingArg(int v,String h){
		viewNum=v;
		hostPort=h;
	}
}
