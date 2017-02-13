package uta.shan.ds.common;
import java.io.Serializable;
import uta.shan.ds.common.Type;
public class PutAppendArg implements Serializable{
	static final long serialVersionUID = 39L;
	public String key;
	public String value;
	public String me;
	public Type flag;
	public String rid;
	public PutAppendArg(String me,String key,String val,Type flag,String rid){
		this.me=me;
		this.key=key;
		this.value=val;
		this.flag=flag;
		this.rid=rid;
	}

}

	
