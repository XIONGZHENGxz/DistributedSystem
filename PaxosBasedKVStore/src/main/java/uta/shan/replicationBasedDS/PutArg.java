package uta.shan.ds;
import java.io.Serializable;
public class PutArg implements Serializable{
	static final long serialVersionUID = 39L;
	String key;
	String value;
//	String me;
//	String flag;
	String rid;
	public PutArg(String key,String val,String rid) {
//		this.me=me;
		this.key=key;
		this.value=val;
//		this.flag=flag;
		this.rid=rid;
	}

}

	
