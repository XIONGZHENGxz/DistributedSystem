package uta.shan.ds.client;

import uta.shan.ds.common.Type;
public class ClientOp{
	public String key;
	public String value;
	public Type type;
	public String rid;
	public String me;
	public ClientOp(String key,String vlaue,Type type,String rid,String me){
		this.key=key;
		this.value=value;
		this.rid=rid;
		this.me=me;
		this.type=type;
	}
}
