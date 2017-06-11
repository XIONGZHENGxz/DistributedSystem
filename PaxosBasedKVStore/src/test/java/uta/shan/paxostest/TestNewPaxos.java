package uta.shan.paxostest;

import uta.shan.paxos2.*;
import org.junit.Test;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

public class TestNewPaxos {
	//test single paxosos
	private static final String LOCALHOST="localhost";
	private static final int NUMTHREADS = 10;
	private int num;
	private String[] peers;
	private Paxos<String>[] paxos;
	private int[] ports;

	private ExecutorService es = Executors.newFixedThreadPool(NUMTHREADS);

	//init paxosos
	public void initPaxos(int num){
		this.num=num;
		peers=new String[num];
		ports=new int[num];
		paxos=new Paxos[num];
		for(int i=0;i<num;i++){
			ports[i]=1099+i;
			peers[i]=LOCALHOST;
		}
		for(int i=0;i<num;i++){
			paxos[i]=new Paxos<>(this.peers,this.ports,i);
		}
	}

	public  int numOfDecided(int totalNum,int seq){
		Object value=new Object();
		Status s=null;
		int count=0;
		for(int i=0;i<totalNum;i++){
			Instance inst = (Instance) paxos[i].getLearner().getSeqMap().get(seq);
			if(inst != null){
				s = inst.getStatus();
				Object dVal=inst.getProposal().getValue();
				if(s == Status.DECIDED){
					if(count>0 && !value.equals(dVal)) return -1;
					count++;
					value = dVal;
				}
			}
		}
		return count;
	}

	//wait paxos to decide
	public boolean waitDecide(int totalNum,int seq){
		int waitInterval=1000;
		int iter = 30;
		int num = -1;
		while(iter-->0){
			num = numOfDecided(totalNum,seq);
			if(num>=totalNum/2+1) return true;
			try{
				Thread.sleep(waitInterval);
			} catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		return false;
	}

	//test single paxos server
	@Test
	public void test1(){
		int n=1;
		initPaxos(n);
		paxos[0].startConcensus(0,"hello");
		paxos[0].run();
		assertTrue(waitDecide(n,0));
	}

	//multiple proposer
	@Test
	public void test2(){
		int n=2;
		initPaxos(n);
		paxos[0].startConcensus(0,"hello");
		paxos[1].startConcensus(0,"lang");
		for(int i=0;i<n;i++) es.execute(paxos[i]);
		boolean ok=false;
		try{
			es.shutdown();
			ok=es.awaitTermination(1,TimeUnit.MINUTES);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		assertTrue(ok);
		assertTrue(waitDecide(n,0));
	}

	@Test
	public void test3(){
		int n=5;
		this.initPaxos(n);
		paxos[0].startConcensus(0,"hello");
		paxos[1].startConcensus(0,"lang");
		paxos[2].startConcensus(0,"zheng");
		paxos[3].startConcensus(1,"xiong");
		paxos[4].startConcensus(1,"xz");
		for(int i=0;i<n;i++) es.execute(paxos[i]);
		boolean ok=false;
		try{
			es.shutdown();
			ok=es.awaitTermination(1,TimeUnit.MINUTES);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		assertTrue(ok);
		assertTrue(waitDecide(n,0));
	}
}
