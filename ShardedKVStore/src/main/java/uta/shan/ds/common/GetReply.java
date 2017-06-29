package uta.shan.ds.common;
import java.io.Serializable;
public class GetReply implements Serializable{
	private final static long serialVersionUID=11L;
	public String value;
	public boolean status;
	public GetReply(){
		status=false;
	}
	public GetReply(String val,boolean status){
		value=val;
		this.status=status;
	}

}
