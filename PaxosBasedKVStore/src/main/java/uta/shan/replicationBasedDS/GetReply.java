package uta.shan.ds;
import java.io.Serializable;
public class GetReply implements Serializable{
	private final static long serialVersionUID=11L;
	String value;
	boolean status;
	public GetReply(){
		status=false;
	}
	public GetReply(String val,boolean status){
		value=val;
		this.status=status;
	}

}
