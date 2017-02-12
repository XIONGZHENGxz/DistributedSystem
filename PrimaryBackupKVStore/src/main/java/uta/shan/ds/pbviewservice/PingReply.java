import java.io.Serializable;
public class PingReply implements Serializable{
	static final long serialVersionUID = 1L;
	View view;
	boolean err;
	public PingReply(View v,boolean f){
		view=v;
		err=f;
	}
}
