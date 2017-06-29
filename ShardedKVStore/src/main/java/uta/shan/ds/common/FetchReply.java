package uta.shan.ds.common;

public class FetchReply{
	Map<String,String> data;
	Err err;
	Map<String,String> preReply;
	public FetchReply(Map<String,String> data,Err err,Map<String,String> preReply){
		this.data=data;
		this.err=err;
		this.preReply=preReply;
	}

}
