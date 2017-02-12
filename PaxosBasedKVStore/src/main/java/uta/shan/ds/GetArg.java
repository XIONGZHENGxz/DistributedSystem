package uta.shan.ds;
import java.io.Serializable;

public class GetArg implements Serializable{
	static final long serialVersionUID = 1L;
	String key;
	String rid;
	String me;
	public GetArg(String key,String rid,String me){
		this.key=key;
		this.rid=rid;
		this.me=me;
	}
}


