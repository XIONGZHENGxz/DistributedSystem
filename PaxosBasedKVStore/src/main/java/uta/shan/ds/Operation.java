package uta.shan.ds;
import java.io.Serializable;
public class Operation implements Serializable{
	int seq;//sequence number 
	int id;//0 is get, 1 is put 2 is append;
	String key;
	String value;
	public Operation(){}
	public Operation(String key,String value,int id,int seq){
		this.id=id;
		this.key=key;
		this.value=value;
		this.seq=seq;
	}

}
