package uta.shan.replicationBasedDS;
import java.io.Serializable;
public class Operation<K,V> implements Serializable{
	private String type;//operation type,get,put,append
	private K key;
	private V value;
	private String rid;
	public Operation(){}

	public Operation(String rid,K key,V value,String t){
		this.rid=rid;
		this.type=t;
		this.key=key;
		this.value=value;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	public String getType() {
		return type;
	}

	public String getRid() {
		return rid;
	}

}
