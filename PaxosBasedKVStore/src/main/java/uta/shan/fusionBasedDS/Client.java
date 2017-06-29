package uta.shan.fusionBasedDS;

/**
 * Created by xz on 6/7/17.
 */
import com.sun.javafx.collections.MappingChange;
import uta.shan.communication.Messager;
import uta.shan.config.ConfigReader;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;

public class Client {
    private String me;//host name
    private String[] servers;//server ip
    private int[] ports;//server port

    public Client(String me, String[] servers, int[] ports) {
        this.me = me;
        this.servers = servers;
        this.ports = ports;
    }

    public String getMe() {
        return this.me;
    }

    public int get(int key) {
        int ind = decideServer(key);
        String request = "get "+Integer.toString(key);
        String reply = Messager.sendAndWaitReply(request,servers[ind],ports[ind]);
        return Integer.parseInt(reply);
    }

    public String put(int key, int value) {
        int ind = decideServer(key);
        System.out.println("putting....");
        String request = "put "+Integer.toString(key)+" "+Integer.toString(value);
        String reply = Messager.sendAndWaitReply(request,servers[ind],ports[ind]);
        return reply;
    }

    public String remove(int key) {
        int ind = decideServer(key);
        String request = "remove "+Integer.toString(key);
        String reply = Messager.sendAndWaitReply(request,servers[ind],ports[ind]);
        return reply;
    }

    public void doOperation(String op, Map<Integer,Integer> store) {
        System.out.println(op);
        StringTokenizer st = new StringTokenizer(op);
        String res = "";
        String arg =st.nextToken();
        if(arg.equals("put")) {
            int key = Integer.parseInt(st.nextToken());
            int value = Integer.parseInt(st.nextToken());
            res = put(key,value);
            store.put(key,value);
        } else if(st.equals("get")) {
            res = "get" + String.valueOf(get(Integer.parseInt(st.nextToken())));
        } else if(st.equals("remove")) {
            int key = Integer.parseInt(st.nextToken());
            store.remove(key);
        }
        System.out.println(res);
    }

    public int decideServer(int key) {
        return key%servers.length;
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
        System.out.println(me);
        Client client = new Client(me, primaryHosts, primaryPorts);
        List<String> ops = new ArrayList<>();
        ConfigReader.readOperations(args[1],ops);
        System.out.println(ops.size());
        for(String op: ops) {
            client.doOperation(op,store);
        }
        for(int key: store.keySet()) {
            if(client.get(key) == store.get(key)) {
                System.out.println("match");
            } else {
                try {
                    throw new Exception("key "+key+" not match");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
