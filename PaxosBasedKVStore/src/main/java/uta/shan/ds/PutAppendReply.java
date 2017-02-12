package uta.shan.ds;
import java.io.Serializable;
public class PutAppendReply implements Serializable{
	private final static long serialVersionUID=10L;
	boolean status;
	public PutAppendReply(boolean b){
		status=b;
	}
}

