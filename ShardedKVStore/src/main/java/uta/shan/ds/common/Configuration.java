package uta.shan.ds.common;
public class Configuration{
	int configNumber;//config number
	int[] shards;//shard->gid
	Map<Integer,String> Groups;//gid->servers

	public Configuration(int config,int[] sha){
		configNumber=config;
		shards=sha;
		Groups=new HashMap<>();
	}
}
