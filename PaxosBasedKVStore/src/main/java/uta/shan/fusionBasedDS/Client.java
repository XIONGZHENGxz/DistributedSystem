package uta.shan.fusionBasedDS;

/**
 * Created by xz on 6/7/17.
 */
import uta.shan.common.InputGenerator;
import uta.shan.communication.Messager;
import uta.shan.communication.Util;
import uta.shan.config.ConfigReader;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;


public class Client<K,V> {
    private String me;//host name
    private String[] servers;//server ip
    private int[] ports;//server port
    private String[] fusedServers;
    private int[] fusedPorts;

    public Client(String me, String[] servers, int[] ports, String[] fusedServers, int[] fusedPorts) {
        this.me = me;
        this.servers = servers;
        this.ports = ports;
        this.fusedServers = fusedServers;
        this.fusedPorts = fusedPorts;
    }

    public String getMe() {
        return this.me;
    }

    public V get(K key) {
        int ind = decideServer(key);
        Request<K,V> request = new Request<>(RequestType.GET, key);
        Reply<V> reply = (Reply<V>) Messager.sendAndWaitReply(request,servers[ind],ports[ind]);
        if(reply == null || reply.getStatus() != Status.OK) return null;
        return reply.getVal();
    }

    public Status put(K key, V value) {
        int ind = decideServer(key);
        Request<K,V> request = new Request<>(RequestType.PUT, key, value);
        Reply<V> reply = (Reply<V>) Messager.sendAndWaitReply(request,servers[ind],ports[ind]);
        if(reply == null) return Status.ERR;
        return reply.getStatus();
    }

    public Status remove(K key) {
        int ind = decideServer(key);
        Request<K,V> request = new Request<>(RequestType.REMOVE, key);
        Reply<V> reply = (Reply<V>) Messager.sendAndWaitReply(request,servers[ind],ports[ind]);
        return reply.getStatus();
    }

    public void shutDown(int id) {
        Request<K,V> request = new Request<>(RequestType.DOWN, null);
        Messager.sendMsg(request, servers[id], ports[id]);
    }

    //resume server
    public void resume(int id) {
        Messager.sendMsg("resume",servers[id],ports[id]);
    }

    public Status doOperation(String arg, K key, V value, Map<K,V> store) {
        if(arg.equals("put")) {
            Status stats = put(key,value);
            store.put(key,value);
            return stats;
        } else if(arg.equals("get")) {
            return Status.OK;
        } else if(arg.equals("remove")) {
            Status ok = remove(key);
            if(Util.DEBUG) System.out.println("remove "+key.toString()+ " "+ok.toString());
            store.remove(key);
            return ok;
        } else if(arg.equals("down")) {
            int id = (Integer) value;
            shutDown(id);
            return Status.OK;
        }
        return Status.OK;
    }

    public int decideServer(K key) {
        if(key instanceof Integer) {
            int k = (Integer) key;
            return k % servers.length;
        }
        else return 0;
    }

    //compare
    public boolean compare(Map<K,V> store) {
        for(K key: store.keySet()) {
            if(get(key) != store.get(key)) {
                return false;
            }
        }
        return true;
    }

    //check recover
    public boolean checkRecover(FusionHashMap[] primaries, Map<K, V> store) {
        for(K key: store.keySet()) {
            int ind = decideServer(key);
            V val = (V)primaries[ind].get(key);
            if(Util.DEBUG) System.out.println(val+ " "+store.get(key));
            if (!val.equals(store.get(key))) return false;
        }
        return true;
    }

    //test fusion ds
    public static void main(String...args) {
        Map<Integer,Integer> store = new HashMap<>();
        String me = "";
        try {
            me = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        int[] nums = new int[2];
        ConfigReader.readNumbers(args[0],nums);
        String[] primaryHosts = new String[nums[0]];
        int[] primaryPorts = new int[nums[0]];
        String[] fusedHosts = new String[nums[1]];
        int[] fusedPorts = new int[nums[1]];

        ConfigReader.readJson(args[0],primaryHosts,fusedHosts,primaryPorts, fusedPorts);
        Client<Integer, Integer> client = new Client(me, primaryHosts, primaryPorts,fusedHosts,fusedPorts);
        List<String> ops = new ArrayList<>();
        ConfigReader.readOperations(args[1],ops);
        long start = Util.getCurrTime();
        for(int i = 0;i < ops.size(); i++) {
            StringTokenizer st = new StringTokenizer(ops.get(i));
            String arg = st.nextToken();
            int key = Integer.parseInt(st.nextToken());
            int val = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : -1;
            client.doOperation(arg, key, val,store);
        }
        long end = Util.getCurrTime();
        System.out.println("Update time: "+(end-start));
        Random rand = new Random();
        client.doOperation("down",null, rand.nextInt(nums[0]), store);
        FusionHashMap[] primaries = Fusion.recover(primaryHosts,fusedHosts,primaryPorts,fusedPorts);
        boolean ok = client.checkRecover(primaries,store);
        System.out.println("recover result: "+ok);
    }
}
