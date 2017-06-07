package uta.shan.ds;
import java.io.Serializable;
public class Operation implements Serializable{
	String type;//operation type,get,put,append
	String key;
	String value;
	String rid;
	public Operation(){}
	public Operation(String rid,String key,String value,String t){
		this.rid=rid;
		this.type=t;
		this.key=key;
		this.value=value;
	}

}
