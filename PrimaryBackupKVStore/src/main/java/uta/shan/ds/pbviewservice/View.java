import java.io.Serializable;
public class View implements Serializable{
	static final long serialVersionUID = 43L;
	int viewNum;
	String primary;
	String backup;
	public View(int v,String p,String b){
		viewNum=v;
		primary=p;
		backup=b;
	}

}
