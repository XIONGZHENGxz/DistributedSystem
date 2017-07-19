package uta.shan.zookeeperBasedDS;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import uta.shan.common.InputGenerator;
import uta.shan.communication.Util;
import uta.shan.config.ConfigReader;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by xz on 7/12/17.
 */
public class Client<K,V> {
    private String[] hosts;
    private ZooKeeper zk;
    private DataType type;
    private ZookeeperConnection conn;

    public Client(String[] hosts, DataType type) {
        this.hosts = hosts;
        this.type = type;
        conn = new ZookeeperConnection();
    }

    //serialize
    public byte[] serialize(Object object) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    public V deserialize(byte[] bytes) {

        if(type == DataType.STRING ) {
            return (V)bytesToString(bytes);
        }
        else if(type == DataType.INTEGER) {
            return (V) bytesToInt(bytes);
        }
        else {
            System.out.println("not any type....");
            return null;
        }
    }

    //convert value to byte array
    public byte[] convertToBytes(V val) {
        if(val instanceof String) {
            return ((String) val).getBytes();
        } else if(val instanceof Integer) {
            return ByteBuffer.allocate(4).putInt((Integer) val).array();
        } else if(val instanceof Long) {
            return ByteBuffer.allocate(8).putLong((Long) val).array();
        } else return serialize(val);
    }

    public String bytesToString(byte[] bytes) {
        String res = "";
        try {
            res = new String(bytes,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return res;
    }

    public Integer bytesToInt(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        int val = bb.getInt();
        return val;
    }

    public boolean put(K key, V val) {
        String keyStr = key.toString();
        String path = Util.root + "/" + keyStr;
        byte[] data = convertToBytes(val);
        if(Util.DEBUG) System.out.println("create path: "+path);
        boolean res = false;
        for(String host : hosts) {
            try {
                conn.reset();
                zk = conn.connect(host);
                //check exist
                Stat stat = zk.exists(path, false);
                if(stat == null) {
                    zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
                else {
                    zk.setData(path, data, stat.getVersion());
                }
                conn.close();
                return true;
            } catch (IOException | InterruptedException | KeeperException e) {
                e.printStackTrace();
            } finally {
                try {
                    conn.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            conn.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public V get(K key) {
        String keyStr = key.toString();
        String path = Util.root + "/" + keyStr;
        V res = null;
        for(String host : hosts) {
            try {
                conn.reset();
                zk = conn.connect(host);
                Stat stat = zk.exists(path, false);
                if(stat != null) {
                   byte[] bytes = zk.getData(path, false, null);
                   res = deserialize(bytes);
                   conn.close();
                   return res;
                }
            } catch (IOException | InterruptedException | KeeperException e) {
                e.printStackTrace();
            } finally {
                try {
                    conn.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            conn.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }

    public boolean remove(K key) {
        String keyStr = key.toString();
        String path = Util.root + "/" + keyStr;
        boolean res = false;
        for(String host : hosts) {
            try {
                conn.reset();
                zk = conn.connect(host);
                Stat stat = zk.exists(path, false);
                if(stat != null) {
                    zk.delete(path, stat.getVersion());
                    res = true;
                    conn.close();
                    return res;
                }
            } catch (IOException | KeeperException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    conn.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            conn.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public V doOperation(String arg, K key, V val, Map<K,V> store) {

        if(arg.equals("put")) {
            boolean ok = put(key, val);
            if(Util.DEBUG) System.out.println("put status : "+ok);
            store.put(key,val);
        } else if(arg.equals("get")) {
            return get(key);
        } else if(arg.equals("remove")) {
            boolean ok = remove(key);
            store.remove(key);
        }
        return null;
    }

    public boolean checkConsistency(Map<K, V> store) {
        for(K key : store.keySet()) {
            V val = get(key);
            System.out.println("key : "+key.toString() + " storeval: "+store.get(key) + "keeperval: "+val);
            if(!store.get(key).equals(val)) return false;
        }
        return true;
    }

    public static void main(String...args) {
        Map<Integer, Integer> store = new HashMap<>();
        List<String> ops = new ArrayList<>();
        int[] numHosts = new int[1];
        ConfigReader.readNumHosts(args[0], numHosts);
        String[] hosts = new String[numHosts[0]];
        ConfigReader.readServers(args[0],hosts);
        Client<Integer, Integer> client = new Client<>(hosts,DataType.INTEGER);
        if(Util.DEBUG) System.out.println("hosts: "+hosts[0] + hosts[1] + hosts[2]);
        ConfigReader.readOperations(args[1],ops);
        long start = Util.getCurrTime();

        for(int i = 0; i < ops.size(); i++) {
            StringTokenizer st = new StringTokenizer(ops.get(i));
            String arg = st.nextToken();
            int key = Integer.parseInt(st.nextToken());
            if(arg.equals("put")) {
                int val = Integer.parseInt(st.nextToken());
                client.doOperation(arg, key, val, store);
            } else {
                client.doOperation(arg, key, -1, store);
            }
        }

        long end = Util.getCurrTime();
        System.out.println("update time: " + (end - start));
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {

        }
        boolean cons = client.checkConsistency(store);
        System.out.println("consistency result: "+cons);
    }
}
