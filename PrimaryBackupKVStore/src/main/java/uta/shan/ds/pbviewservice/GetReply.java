import java.io.Serializable;
public class GetReply<T> implements Serializable{
	private final static long serialVersionUID=11L;
	T value;
	public GetReply(T val){
		value=val;
	}

}
