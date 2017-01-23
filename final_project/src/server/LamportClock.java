public class LamportClock{
	int time;
	public LamportClock(){
		time=1;
	}

	public void tick(){
		time++;
	}

	public void sendAction(){
		time++;
	}

	public void receiveAction(int val){
		time=Math.max(time,val)+1;
	}
}
