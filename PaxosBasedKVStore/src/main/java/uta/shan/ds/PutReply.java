package uta.shan.ds;
import java.io.Serializable;
public class PutReply implements Serializable{
	private final static long serialVersionUID=10L;
	private boolean status;
	public PutReply(boolean b){
		status=b;
	}
	public boolean getStatus() {
		return this.status;
	}
}

