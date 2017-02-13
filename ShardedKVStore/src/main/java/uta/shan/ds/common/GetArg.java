package uta.shan.ds.common;
import java.io.Serializable;

public class GetArg implements Serializable{
	static final long serialVersionUID = 1L;
	public String key;
	public String rid;
	public String me;
	public GetArg(String key,String rid,String me){
		this.key=key;
		this.rid=rid;
		this.me=me;
	}
}


