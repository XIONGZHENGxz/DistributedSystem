package kvpaxos;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import paxos.*;
/**
 * Created by Colinhu on 10/30/17.
 */
public class Server implements KVPaxosRMI{

    ReentrantLock mutex;
    Registry registry;
    Paxos px;
    int me;

    // Your definitions here
    Map<String, Integer> kv;
    Map<Integer, Boolean> seq;
    List<Op> logs;
    int lastSeq;
    String[] servers;
    int[] ports;
    KVPaxosRMI stub;

    public Server(String[] servers, int[] ports, int me){
        this.me = me;
        this.servers = servers;
        this.ports = ports;
        kv = new HashMap<>();
        seq = new HashMap<>();
        this.lastSeq = 1;
        this.logs = new ArrayList<>();

        this.mutex = new ReentrantLock();
        this.px = new Paxos(me, servers, ports);
        try{
            System.setProperty("java.rmi.server.hostname", this.servers[this.me]);
            registry = LocateRegistry.getRegistry(this.ports[this.me]);
            stub = (KVPaxosRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("KVPaxos", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public Op wait(int seq){
        int to = 10;
        while(true){
            Paxos.retStatus ret = this.px.Status(seq);
            if(ret.state == State.Decided){
                return Op.class.cast(ret.v);
            }
            try{
                Thread.sleep(to);
            } catch (Exception e){
                e.printStackTrace();
            }
            if( to < 1000){
                to = to * 2;
            }
        }
    }


    public void Apply(Op v){
        if(v.op != "Get"){
            this.logs.add(v);
            this.kv.put(v.key, v.value);
        }

        this.seq.put(v.ClientSeq, true);
        this.px.Done(this.lastSeq);
        this.lastSeq++;
    }

    public void ProcessOperation(Op v){
        boolean ok = false;
        Op log;
        while(!ok){
            int seq = this.lastSeq;

            Paxos.retStatus ret = this.px.Status(this.lastSeq);
            if(ret.state == State.Decided){
                log = Op.class.cast(ret.v);
            }
            else{
                this.px.Start(seq, v);
                log = this.wait(seq);

            }
            ok = (v.ClientSeq == log.ClientSeq);
            this.Apply(log);
        }
    }

    public Response Get(Request req){
        this.mutex.lock();
        try{
            Op v = new Op("Get", req.seq, req.key, null);
            this.ProcessOperation(v);

            Response rsp = new Response();
            if(this.kv.containsKey(v.key)){
                rsp.value = this.kv.get(v.key);
                rsp.ok = true;
            }
            return rsp;
        } finally {
            this.mutex.unlock();
        }
    }

    public Response Put(Request req){
        this.mutex.lock();
        try{

            Response rsp = new Response();
            if(this.seq.containsKey(req.seq)){
                rsp.ok = true;
                return rsp;
            }
            Op v = new Op("Put", req.seq, req.key, req.value);
            this.ProcessOperation(v);
            return rsp;
        } finally {
            this.mutex.unlock();
        }
    }


}
