package uta.shan.ds.common;
import java.io.Serializable;
public class PutAppendReply implements Serializable{
	private final static long serialVersionUID=10L;
	public boolean status;
	public PutAppendReply(boolean b){
		status=b;
	}
}

