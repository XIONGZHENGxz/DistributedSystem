package uta.shan.ds;
import java.io.Serializable;
public class PutAppendArg implements Serializable{
	static final long serialVersionUID = 39L;
	String key;
	String value;
	String me;
	String flag;
	String rid;
	public PutAppendArg(String me,String key,String val,String flag,String rid){
		this.me=me;
		this.key=key;
		this.value=val;
		this.flag=flag;
		this.rid=rid;
	}

}

	
