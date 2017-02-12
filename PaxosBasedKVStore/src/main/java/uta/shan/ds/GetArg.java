package uta.shan.ds;
import java.io.Serializable;

public class GetArg implements Serializable{
	static final long serialVersionUID = 1L;
	String key;
	String rid;
	public GetArg(String k,String rid){
		key=k;
		this.rid=rid;
	}
}


