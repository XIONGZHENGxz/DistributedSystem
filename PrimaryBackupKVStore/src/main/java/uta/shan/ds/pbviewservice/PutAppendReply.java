import java.io.Serializable;
public class PutAppendReply implements Serializable{
	private final static long serialVersionUID=10L;
	boolean err;
	public PutAppendReply(boolean b){
		err=b;
	}
}

