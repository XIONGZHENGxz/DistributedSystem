package uta.shan.ds.common;

import java.util.List;
import java.util.ArrayList;
public class JoinArg{
	public int gid;//groupd Id
	public List<String> servers;// group of servers
	public int[] ports;//corresponding ports
	public JoinArg(int g,List<String> s,int[] p){
		gid=g;
		servers=new ArrayList<>(s);
		ports=p;
	}
}
