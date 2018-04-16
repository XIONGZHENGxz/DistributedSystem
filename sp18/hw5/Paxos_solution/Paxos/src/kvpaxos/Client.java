package kvpaxos;


import paxos.PaxosRMI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

/**
 * Created by Colinhu on 10/30/17.
 */
public class Client {
    String[] servers;
    int[] ports;
    Random rand;


    public Client(String[] servers, int[] ports){
        this.servers = servers;
        this.ports = ports;
        rand = new Random();
    }


    public Response Call(String rmi, Request req, int id){
        Response callReply = null;
        KVPaxosRMI stub;
        try{
            Registry registry= LocateRegistry.getRegistry(this.ports[id]);
            stub=(KVPaxosRMI) registry.lookup("KVPaxos");
            if(rmi.equals("Get"))
                callReply = stub.Get(req);
            else if(rmi.equals("Put")){
                callReply = stub.Put(req);}
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return callReply;
    }

    public Integer Get(String key){
        Request req = new Request(key, null, this.rand.nextInt(Integer.MAX_VALUE));

        Response rsp = new Response();
        int index = 0;
        while(true){
            rsp = Call("Get", req, index);
            if(rsp != null && rsp.ok){
                return rsp.value;
            }
            try{
                Thread.sleep(2000);
            } catch (Exception e){
                e.printStackTrace();
            }
            index = (index + 1) % this.servers.length;
        }

    }

    public boolean Put(String key, Integer value){
        Request req = new Request(key, value, this.rand.nextInt(Integer.MAX_VALUE));
        Response rsp = new Response();

        int index = 0;
        while(true){
            rsp = Call("Put", req, index);
            if(rsp != null && rsp.ok){
                break;
            }
            try{
                Thread.sleep(2000);
            } catch (Exception e){
                e.printStackTrace();
            }
            index = (index + 1) % this.servers.length;
        }
        return rsp.ok;
    }
}
