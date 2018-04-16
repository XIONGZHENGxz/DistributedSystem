package paxos;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;



public class Paxos implements PaxosRMI, Runnable{

    ReentrantLock mutex;
    String[] peers; // hostname
    int[] ports; // host port
    int me; // index into peers[]

    public Registry registry;
    PaxosRMI stub;

    AtomicBoolean dead;// for testing
    AtomicBoolean unreliable;// for testing
    int rpcCount;// for testing


    // Your data here.
    Map<Integer, Instance> map;
    int[] dones;
    int seq;
    Object value;


    /**
     * Call the constructor to create a Paxos peer.
     * The hostnames of all the Paxos peers (including this one)
     * are in peers[]. The ports are in ports[].
     */
    public Paxos(int me, String[] peers, int[] ports){
        // Your initialization code here
        this.me = me;
        this.peers = peers;
        this.ports = ports;
        this.dead = new AtomicBoolean(false);
        this.unreliable = new AtomicBoolean(false);

        this.map = new HashMap();
        this.mutex = new ReentrantLock();
        this.dones = new int[this.peers.length];
        for(int i = 0; i < this.dones.length; i++){
            this.dones[i] = -1;
        }
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[this.me]);
            registry = LocateRegistry.createRegistry(this.ports[this.me]);
            stub = (PaxosRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("Paxos", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }




    /**
     * Call() sends an RMI to the RMI handler on server with
     * arguments rmi name, request message, and server id. It
     * waits for the reply and return a response message if
     * the server responded, and return null if Call() was not
     * be able to contact the server.
     *
     * You should assume that Call() will time out and return
     * null after a while if it doesn't get a reply from the server.
     *
     * Please use Call() to send all RMIs and please don't change
     * this function.
     */
    public Response Call(String rmi, Request req, int id){
        Response callReply = null;
        PaxosRMI stub;
        try{
            Registry registry=LocateRegistry.getRegistry(this.ports[id]);
            stub=(PaxosRMI) registry.lookup("Paxos");
            if(rmi.equals("Prepare"))
                callReply = stub.Prepare(req);
            else if(rmi.equals("Accept"))
                callReply = stub.Accept(req);
            else if(rmi.equals("Decide"))
                callReply = stub.Decide(req);
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return callReply;
    }

    /**
     * The application wants Paxos to start agreement on instance seq,
     * with proposed value v. Start() should start a new thread to run
     * Paxos on instance seq. Multiple instances can be run concurrently.
     *
     * Hint: You may start a thread using the runnable interface of
     * Paxos object. One Paxos object may have multiple instances, each
     * instance corresponds to one proposed value/command. Java does not
     * support passing arguments to a thread, so you may reset seq and v
     * in Paxos object before starting a new thread. There is one issue
     * that variable may change before the new thread actually reads it.
     * Test won't fail in this case.
     *
     * Start() just starts a new thread to initialize the agreement.
     * The application will call Status() to find out if/when agreement
     * is reached.
     */
    public void Start(int seq, Object v){
        this.seq = seq;
        this.value = v;
        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void run(){
        //Your code here
        if(this.seq < this.Min()){
            return;
        }

        this.Proposer(this.seq, this.value);

    }


    public void Proposer(int seq, Object value){
        while(true){
            Response rsp = this.sendPrepare(seq, value);
            boolean OK = false;
            if(rsp.OK){
                OK = this.sendAccept(seq, rsp.N, rsp.V);
            }
            if(OK){
                this.sendDecide(seq, rsp.N, rsp.V);
                break;
            }

            retStatus ret = this.Status(seq);
            if(ret.state == State.Decided){
                break;
            }
        }

    }

    // RPC handler
    public Response Prepare(Request req){
        this.mutex.lock();
        if(!this.map.containsKey(req.seq)){
            Instance ins = new Instance();
            this.map.put(req.seq, ins);
        }
        Instance ins = this.map.get(req.seq);

        Response rsp = new Response();
        if(req.N > ins.max_prepare){
            // update proposer number
            ins.max_prepare = req.N;
            // if ok, reply accept num and value
            rsp.OK = true;
            rsp.N = ins.max_accept;
            rsp.V = ins.v_accept;
        }
        else{
            rsp.OK = false;
        }
        this.mutex.unlock();
        return rsp;
    }

    public Response Accept(Request req){
        this.mutex.lock();
        if(!this.map.containsKey(req.seq)){
            Instance ins = new Instance();
            this.map.put(req.seq, ins);
        }
        Instance ins = this.map.get(req.seq);

        Response rsp = new Response();

        if(req.N >= ins.max_prepare){
            ins.max_accept = req.N;
            ins.v_accept = req.V;
            ins.max_prepare = req.N;
            rsp.OK = true;
        }
        else{
            rsp.OK = false;
        }
        this.mutex.unlock();
        return rsp;
    }

    public Response Decide(Request req){
        this.mutex.lock();
        if(!this.map.containsKey(req.seq)){
            Instance ins = new Instance();
            this.map.put(req.seq, ins);
        }
        Instance ins = this.map.get(req.seq);

        ins.v_accept = req.V;
        ins.max_prepare = req.N;
        ins.max_accept = req.N;
        ins.state = State.Decided;

        this.dones[req.me] = req.done;

        Response rsp = new Response();
        this.mutex.unlock();
        return rsp;

    }

    public Response sendPrepare(int seq, Object value){
        int pnum = this.generatePNum(seq);
        Request req = new Request(seq, pnum, value);
        int counter = 0;


        int replyPnum = Integer.MIN_VALUE;
        Object replyValue = value;

        for(int i = 0; i < this.peers.length; i++){
            Response rsp;
            if(i == this.me){
                rsp = this.Prepare(req);
            }
            else{

                rsp = this.Call("Prepare", req, i);
                // call(peer, "", &arg, &reply);
            }
            if(rsp != null && rsp.OK == true){
                counter++;
                if(rsp.N > replyPnum){
                    replyPnum = rsp.N;
                    replyValue = rsp.V;
                }
            }
        }


        Response rsp = new Response();
        if(counter >= this.majority()){
            rsp.OK = true;
            rsp.N = pnum;
            rsp.V = replyValue;
        }
        return rsp;
    }


    public boolean sendAccept(int seq, int Pnum, Object value){


        int counter = 0;
        for(int i = 0; i < this.peers.length; i++){
            Response rsp;
            if(i == this.me){
                rsp = Accept(new Request(seq, Pnum, value));
            }
            else{
                rsp = this.Call("Accept", new Request(seq, Pnum, value), i);
                //call();
            }

            if(rsp != null && rsp.OK){
                counter++;
            }
        }

        return counter >= this.majority();
    }

    public void sendDecide(int seq, int Pnum, Object value){
        this.mutex.lock();
        try{
            Instance ins = this.map.get(seq);
            ins.v_accept = value;
            ins.state = State.Decided;
            ins.max_accept = Pnum;
            ins.max_prepare = Pnum;

        } finally {
            this.mutex.unlock();
        }

        for(int i = 0; i < this.peers.length; i++){
            Response rsp;
            if(i == this.me){
                continue;
            }
            else {
                //dones
                int done = this.dones[this.me];
                rsp = this.Call("Decide", new Request(seq, Pnum, value, done, this.me), i);
                //call();
            }
        }

    }

    private int majority(){
        return this.peers.length/2 + 1;
    }



    /**
     * The application on this machine is done with
     * all instances <= seq.
     *
     * see the comments for Min() for more explanation.
     */
    public void Done(int seq) {
        this.mutex.lock();
        try {
            if (seq > this.dones[this.me]) {
                this.dones[this.me] = seq;
            }
        } finally {
            this.mutex.unlock();
        }
    }


    /**
     * The application wants to know the
     * highest instance sequence known to
     * this peer.
     */
    public int Max(){
        this.mutex.lock();
        try{
            int max = 0;
            for(Integer key : this.map.keySet()){
                if(key > max){
                    max = key;
                }
            }
            return max;
        } finally {
            this.mutex.unlock();
        }
    }

    /**
     * Min() should return one more than the minimum among z_i,
     * where z_i is the highest number ever passed
     * to Done() on peer i. A peers z_i is -1 if it has
     * never called Done().

     * Paxos is required to have forgotten all information
     * about any instances it knows that are < Min().
     * The point is to free up memory in long-running
     * Paxos-based servers.

     * Paxos peers need to exchange their highest Done()
     * arguments in order to implement Min(). These
     * exchanges can be piggybacked on ordinary Paxos
     * agreement protocol messages, so it is OK if one
     * peers Min does not reflect another Peers Done()
     * until after the next instance is agreed to.

     * The fact that Min() is defined as a minimum over
     * all Paxos peers means that Min() cannot increase until
     * all peers have been heard from. So if a peer is dead
     * or unreachable, other peers Min()s will not increase
     * even if all reachable peers call Done. The reason for
     * this is that when the unreachable peer comes back to
     * life, it will need to catch up on instances that it
     * missed -- the other peers therefore cannot forget these
     * instances.
     */
    public int Min(){
        // Your code here
        this.mutex.lock();
        try{
            int min = this.dones[this.me];
            for(int i = 0; i < this.dones.length; i++){
                if(this.dones[i] < min){
                    min = this.dones[i];
                }
            }

            for(Iterator<Map.Entry<Integer, Instance>> it = this.map.entrySet().iterator(); it.hasNext();){
                Map.Entry<Integer, Instance> entry = it.next();
                if(entry.getKey() > min){
                    continue;
                }
                if(entry.getValue().state != State.Decided){
                    continue;
                }
                it.remove();
            }

            return min + 1;
        } finally {
            this.mutex.unlock();
        }
    }


    /**
     * the application wants to know whether this
     * peer thinks an instance has been decided,
     * and if so what the agreed value is. Status()
     * should just inspect the local peer state;
     * it should not contact other Paxos peers.
     */
    public retStatus Status(int seq){
        // Your code here
        if(seq < this.Min()){
            return new retStatus(State.Forgotten, null);
        }

        this.mutex.lock();
        try{
            if(!this.map.containsKey(seq)){
                return new retStatus(State.Pending, null);
            }
            else{
                Instance ins = this.map.get(seq);
                return new retStatus(ins.state, ins.v_accept);
            }
        } finally {
            this.mutex.unlock();
        }
    }

    /**
     * helper class for Status() return
     */
    public class retStatus{
        public State state;
        public Object v;

        public retStatus(State state, Object v){
            this.state = state;
            this.v = v;
        }
    }

    /**
     * Tell the peer to shut itself down.
     * For testing.
     * Please don't change these four functions.
     */
    public void Kill(){
        this.dead.getAndSet(true);
        if(this.registry != null){
            try {
                UnicastRemoteObject.unexportObject(this.registry, true);
            } catch(Exception e){
                System.out.println("None reference");
            }
        }
    }

    public boolean isDead(){
        return this.dead.get();
    }

    public void setUnreliable(){
        this.unreliable.getAndSet(true);
    }

    public boolean isunreliable(){
        return this.unreliable.get();
    }


    private int generatePNum(int seq){
        this.mutex.lock();
        if(!this.map.containsKey(seq)){
            Instance ins = new Instance();
            this.map.put(seq, ins);
        }
        Instance ins = this.map.get(seq);
        this.mutex.unlock();
        if(ins.max_prepare == Integer.MIN_VALUE)
            return this.me + 1;
        return (ins.max_prepare / this.peers.length + 1)*this.peers.length + this.me + 1;
    }

    private class Instance {

        int max_prepare; // highest prepare number seen
        int max_accept; // highest accept seen
        State state;
        Object v_accept; //highest-numbered proposal accepted

        public Instance(){
            this.max_prepare = Integer.MIN_VALUE;
            this.max_accept = Integer.MIN_VALUE;
            this.state = State.Pending;
            this.v_accept = null;
        }

    }

    public static void main(String [] args){
        String host = "127.0.0.1";
        int num = 5;
        String[] peers = new String[num];
        int[] ports = new int[num];
        Paxos[] px = new Paxos[num];
        for(int i = 0 ; i < num; i++){
            ports[i] = 1099+i;
            peers[i] = host;
        }

        for(int i = 0; i < num; i++){
            px[i] = new Paxos(i, peers, ports);
        }
        for(int i = 0; i < 1; i++){
            px[i].Start(0, "hello");
        }


    }

}




