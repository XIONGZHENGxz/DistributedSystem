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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;

public class Client {
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

    public void shutDown(int id) {
        String status = Messager.sendAndWaitReply("shut down",servers[id],ports[id]);
        if(Util.DEBUG) System.out.println("shut down result: "+status);
    }

    public void doOperation(String op, Map<Integer,Integer> store) {
        if(Util.DEBUG) System.out.println(op);
        StringTokenizer st = new StringTokenizer(op);
        String res = "";
        String arg =st.nextToken();
        if(arg.equals("put")) {
            int key = Integer.parseInt(st.nextToken());
            int value = Integer.parseInt(st.nextToken());
            res = put(key,value);
            store.put(key,value);
        } else if(arg.equals("get")) {
            res = "get" + String.valueOf(get(Integer.parseInt(st.nextToken())));
        } else if(arg.equals("remove")) {
            int key = Integer.parseInt(st.nextToken());
            store.remove(key);
        } else if(arg.equals("down")) {
            int id = Integer.parseInt(st.nextToken());
            shutDown(id);
            Fusion.recover(servers,fusedServers,ports,fusedPorts);
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
        InputGenerator.generateInput(Integer.parseInt(args[1]),args[2],nums[0]);
        Client client = new Client(me, primaryHosts, primaryPorts,fusedHosts,fusedPorts);
        List<String> ops = new ArrayList<>();
        ConfigReader.readOperations(args[2],ops);
        long start = Util.getCurrTime();
        for(int i=0;i<ops.size()-1;i++) {
            client.doOperation(ops.get(i),store);
        }
        long end = Util.getCurrTime();
        System.out.println("Update time: "+(end-start));
        client.doOperation(ops.get(ops.size()-1),store);
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
