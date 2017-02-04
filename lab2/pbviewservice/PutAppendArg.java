import java.io.Serializable;
public class PutAppendArg<T> implements Serializable{
	static final long serialVersionUID = 39L;
	T key;
	T value;
	String flag;
	public PutAppendArg(T key,T val,String flag){
		this.key=key;
		this.value=val;
		this.flag=flag;
	}

}

	
