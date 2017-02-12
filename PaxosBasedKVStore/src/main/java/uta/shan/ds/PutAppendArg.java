package uta.shan.ds;
import java.io.Serializable;
public class PutAppendArg implements Serializable{
	static final long serialVersionUID = 39L;
	String key;
	String value;
	String flag;
	public PutAppendArg(String key,String val,String flag){
		this.key=key;
		this.value=val;
		this.flag=flag;
	}

}

	
