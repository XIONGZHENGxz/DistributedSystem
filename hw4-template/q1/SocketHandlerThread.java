import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.lang.Thread;
public class SocketHandlerThread extends Thread{
	Socket socket;
	Server server;
	BufferedWriter out;
	BufferedReader in;	
	SocketHandlerThread(Socket socket,Server s){
		this.socket=socket;
		this.server=s;
		try{
			in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	public void run(){
		System.out.println("socket handler started...");
		try{
			String req="";
//			char[] inbuf=new char[1024];
//			in.read(inbuf,0,1024);
			req=in.readLine();
			System.out.println("message..."+req);
			if(req.startsWith("ack")){
				server.numAcks++;
				System.out.println("server acks..."+server.numAcks);
			}else if(req.startsWith("release")){
				server.release(req);
			}else if(req.startsWith("request")){
				String[] reqs=req.split(",");
				System.out.println("..."+reqs[1]);
				int id=Integer.parseInt(reqs[1].trim());
				InetSocketAddress addr=new InetSocketAddress(server.servers.get(id-1),server.ports.get(id-1));
				sendMsg("ack",addr);
			}else{
				Request r=new Request(req,server.lc.getValue(),server.myID);
				server.q.offer(r);
				InetSocketAddress addr=(InetSocketAddress) socket.getRemoteSocketAddress();
				HandlerThread thread=new HandlerThread(req,server,addr);
				thread.start();
			}
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	public void sendMsg(String message,InetSocketAddress addr){
		try{
			Socket s=new Socket();
			s.connect(addr);
			out=new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			out.write(message);
			out.write("\n");
			out.flush();
		} catch (IOException e){
			e.printStackTrace();
		} finally{
			try{
				socket.close();
			} catch(IOException e){
				e.printStackTrace();
			}
		}
		System.out.println("sent message..."+message);
	}

}
