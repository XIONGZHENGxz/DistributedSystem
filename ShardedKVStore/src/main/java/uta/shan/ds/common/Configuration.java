package uta.shan.ds.common;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class Configuration{
	private int configNumber;//config number
	private int[] shards;//shard->gid
	private Map<Integer,List<String>> Groups;//gid->servers
	public Configuration(){}
	public Configuration(int config,int[] sha){
		configNumber=config;
		shards=sha;
		Groups=new HashMap<>();
	}

	//return configuration number
	public int getConfigNum(){
		return this.configNumber;
	}

	//return Groups
	public Map<Integer,List<String>> getGroup(){
		return this.Groups;
	}

	//return shards
	public int[] getShards(){
		return this.shards;
	}

	//assign shard to particular group
	public void setShard(int shard,int gid){
		this.shards[shard]=gid;
	}

	//set shards
	public void setShards(int[] shards){
		this.shards=shards;
	}

}
