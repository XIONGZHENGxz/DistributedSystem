package uta.shan.ds.common;
public class JoinArg{
	int gid;//groupd Id
	String[] servers;// group of servers
	int[] ports;//corresponding ports
	public JoinArg(int g,String[] s,int[] p){
		gid=g;
		servers=s;
		ports=p;
	}
}
