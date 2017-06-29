package uta.shan.ds.common;

import java.util.List;
import java.util.ArrayList;

public class Operation{
	public Type type;//operation type
	public int gid;
	public List<String> servers;
	public int shard;
	public Operation(Type t,int g,List<String> servers,int s){
		type=t;
		gid=g;
		servers=new ArrayList<>(servers);
		shard=s;
	}
}
