import org.junit.Test;
import org.junit.Before;
import paxos.*;
import static org.junit.Assert.*;
public class TestPaxos{
	//test single paxos
	private static final String host="192.168.245.150";
	private int num;
	String[] peers;
	Paxos[] pax;
	int[] ports;

	//init paxos
	public void initPaxos(int num){
		this.num=num;
		peers=new String[num];
		ports=new int[num];
		pax=new Paxos[num];
		for(int i=0;i<num;i++){
			ports[i]=1099+i;
			peers[i]=host;
		}
		for(int i=0;i<num;i++){
			pax[i]=new Paxos(i,peers,ports);
		}
	}
	
	public  int numOfDecided(int totalNum,int seq){
		Object value=new Object();
		Status s=null;
		int count=0;
		for(int i=0;i<totalNum;i++){
			if(pax!=null){
				Instance inst=pax[i].getMap().get(seq);
				if(inst!=null){
					s=inst.getStatus();
					Object dVal=inst.getProposal().getValue();
					if(s==Status.DECIDED){
						if(count>0 && !value.equals(dVal))
							return -1;	
						count++;
						value=dVal;
					}
				}
			}
		}
		return count;
	}		

	//wait paxos to decide
	public boolean waitDecide(int totalNum,int seq){
		int waitInterval=1000;
		int iter=30;
		int num=-1;
		while(iter-->0){
			num=numOfDecided(totalNum,seq);
			if(num>=totalNum/2+1) return true;
			try{
				Thread.sleep(waitInterval);
			} catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		return false;
	}
	
	//clean up
	public void cleanup(){
		for(int i=0;i<this.num;i++){
			this.pax[i].cleanup();
		}
	}

	//test single paxos server
	@Test 
	public void test1(){
		int n=1;
		this.initPaxos(n);
		pax[0].start(0,"hello");
		assertTrue(waitDecide(n,0));
		this.cleanup();
	}

	//multiple proposer
	@Test
	public void test2(){
		int n=2;
		this.initPaxos(n);
		pax[0].start(0,"hello");
		pax[1].start(0,"ming");
		assertTrue(waitDecide(n,0));
		this.cleanup();
	}	
}
