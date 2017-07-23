package uta.shan.fusionBasedDSTest;

/**
 * Created by xz on 6/9/17.
 */

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uta.shan.common.InputGenerator;
import uta.shan.communication.Util;
import uta.shan.config.ConfigReader;
import uta.shan.fusionBasedDS.*;

import java.io.IOException;
import java.util.*;

public class SystemTester {
    private static String LOCALHOST = "localhost";
    private static Client<Integer, Integer> client;
    private static PrimaryServer<Integer, Integer>[] primaryServers;
    private static FusedServer<Integer,Integer>[] fusedServers;

    private static String[] fusedHosts;
    private static int[] fusedPorts;
    private static String[] primaryHosts;
    private static int[] primaryPorts;

    private static int numPrimaries = 2;
    private static int numFusedBackups = 2;

    public static void init() {
        fusedHosts = new String[numFusedBackups];
        fusedPorts = new int[numFusedBackups];
        primaryHosts = new String[numPrimaries];
        primaryPorts = new int[numPrimaries];

        fusedServers = new FusedServer[numFusedBackups];
        primaryServers = new PrimaryServer[numPrimaries];

        for(int i=0;i<numFusedBackups;i++) {
            fusedHosts[i] = LOCALHOST;
            fusedPorts[i] = Util.clientPort+i+1;
            fusedServers[i] = new FusedServer(fusedPorts[i],numPrimaries,i);
            while(!fusedServers[i].isBound()) {
                Thread.yield();
            }
        }

        for(int i=0;i<numPrimaries;i++) {
            primaryHosts[i] = LOCALHOST;
            primaryPorts[i] = Util.clientPort+i+numFusedBackups+1;
            primaryServers[i] = new PrimaryServer(fusedHosts,fusedPorts,i,primaryPorts[i]);
            while(!primaryServers[i].isBound()) {
                Thread.yield();
            }
        }
        try {
            Thread.sleep(30);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }

        client = new Client(LOCALHOST,primaryHosts,primaryPorts,fusedHosts,fusedPorts);

    }

    public static void end() {
        for(PrimaryServer ps : primaryServers) {
            ps.shutDown();
        }
        for(FusedServer fs : fusedServers) {
            fs.shutDown();
        }
    }

    @Test
    public void test1() {
        System.out.println(RequestType.APPEND.toString());
        assertTrue(client.getMe() == LOCALHOST);
        assertTrue(client.get(0) == null);
        assertTrue(client.put(2,1) == Status.OK);
        assertTrue(client.get(2) == 1);
        assertTrue(client.put(2,2) == Status.OK);
        assertTrue(client.get(2) == 2);
        assertTrue(client.remove(2) == Status.OK);
        assertTrue(fusedServers[0].getMap().getDataStack().isEmpty());
    }


    @Test
    public void test2() {
        assertTrue(client.put(0,1000) == Status.OK);
        assertTrue(client.get(0) == 1000);
        assertTrue(fusedServers[0].getMap().get(0,0) == 1000);
        assertTrue(client.remove(0) == Status.OK);
        assertTrue(client.remove(10) == Status.NE);
        assertTrue(fusedServers[0].getMap().getDataStack().isEmpty());
    }

    @Test
    public void test3() {
        assertTrue(client.put(0,1000) == Status.OK);
        assertTrue(client.get(0) == 1000);
        assertTrue(client.put(1,500) == Status.OK);
        assertTrue(client.get(1) == 500);
        assertTrue(fusedServers[0].getMap().get(1,1) == 1500);
        assertTrue(fusedServers[0].getMap().get(0,0) == 1500);
    }

    @Test
    public void test4() {
        client.put(0,1000);
        client.put(1,500);
        client.put(2,2000);
        client.put(3,1500);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {

        }
        assertTrue(fusedServers[0].getMap().get(0,2) == 3500);
        assertTrue(fusedServers[0].getMap().getDataStack().getSize() == 2);
        assertTrue(fusedServers[1].getMap().get(0,0) == 2000);
        assertTrue(fusedServers[1].getMap().get(1,1) == 2000);
        assertTrue(fusedServers[1].getMap().get(1,3) == 5000);
        assertTrue(fusedServers[1].getMap().getDataStack().getSize() == 2);
    }

    @Test
    public void test5(){
        client.put(0,1000);
        client.put(1,500);
        client.put(0,2000);
        client.put(1,1000);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {

        }
        assertTrue(fusedServers[0].getMap().get(0,0) == 3000);
        assertTrue(fusedServers[0].getMap().getDataStack().getSize() == 1);
        assertTrue(fusedServers[1].getMap().get(0,0) == 4000);
        assertTrue(fusedServers[1].getMap().get(1,1) == 4000);
        assertTrue(fusedServers[1].getMap().getDataStack().getSize() == 1);
    }
    @Test
    public void test6() {
        client.put(0,1000);
        client.put(1,500);
        client.put(2,2000);
        client.put(3,1500);
        client.remove(2);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {

        }
        assertTrue(fusedServers[0].getMap().get(0, 2) == null);
        assertTrue(fusedServers[0].getMap().getDataStack().getSize() == 2);
        assertTrue(fusedServers[0].getMap().get(1, 3) == 1500);
        assertTrue(fusedServers[1].getMap().get(0, 0) == 2000);
        assertTrue(fusedServers[1].getMap().get(1, 1) == 2000);
        assertTrue(fusedServers[1].getMap().get(1, 2) == null);
        assertTrue(fusedServers[1].getMap().get(1, 3) == 3000);
        assertTrue(fusedServers[1].getMap().getDataStack().getSize() == 2);
    }

    @Test
    public void test7() {
        primaryServers[0].shutDown();
        assertTrue(client.put(0,1) == Status.ERR);
    }

    @Test
    public void test8() {
        client.put(0,1000);
        client.put(1,500);
        client.put(2,2000);
        client.put(3,1500);
        primaryServers[0].shutDown();
        boolean[] flags = new boolean[numPrimaries];
        FusionHashMap[] primaries = Fusion.getPrimaries(primaryHosts,primaryPorts,flags);
        assertTrue(flags[0]==false);
        assertTrue(flags[1]==true);
        boolean[] fusedFlags = new boolean[numFusedBackups];
        FusedMap[] fusedMaps = Fusion.getFused(fusedHosts,fusedPorts,fusedFlags,numPrimaries);
        assertTrue(fusedFlags[0]==true);
        assertTrue(fusedFlags[1]==true);
        FusionHashMap<Integer, Integer>[] data = Fusion.recover(primaryHosts,fusedHosts,primaryPorts,fusedPorts);
        assertTrue(data[0].get(0) == 1000);
        assertTrue(data[0].get(2) == 2000);
        assertTrue(data[1].get(1) == 500);
        assertTrue(data[1].get(3) == 1500);
    }

    @Test
    public void test9() {
        client.put(0,1000);
        client.put(1,500);
        client.put(2,2000);
        client.put(3,1500);
        primaryServers[0].shutDown();
        primaryServers[1].shutDown();
        FusionHashMap<Integer, Integer>[] data = Fusion.recover(primaryHosts,fusedHosts,primaryPorts,fusedPorts);
        assertTrue(data[0].get(0) == 1000);
        assertTrue(data[0].get(2) == 2000);
        assertTrue(data[1].get(1) == 500);
        assertTrue(data[1].get(3) == 1500);
    }

    @Test
    public void test10() {
        client.put(0,1000);
        client.put(1,500);
        client.put(2,2000);
        client.put(3,1500);
        primaryServers[0].shutDown();
        fusedServers[0].shutDown();
        FusionHashMap<Integer, Integer>[] data = Fusion.recover(primaryHosts,fusedHosts,primaryPorts,fusedPorts);
        assertTrue(data[0].get(0) == 1000);
        assertTrue(data[0].get(2) == 2000);
        assertTrue(data[1].get(1) == 500);
        assertTrue(data[1].get(3) == 1500);
    }

    @Test
    public void test11() {
        client.put(0,1000);
        client.put(1,500);
        client.put(2,2000);
        client.put(3,1500);
        primaryServers[1].shutDown();
        fusedServers[0].shutDown();
        FusionHashMap<Integer, Integer>[] data = Fusion.recover(primaryHosts,fusedHosts,primaryPorts,fusedPorts);
        assertTrue(data[0].get(0) == 1000);
        assertTrue(data[0].get(2) == 2000);
        assertTrue(data[1].get(1) == 500);
        assertTrue(data[1].get(3) == 1500);
    }

    @Test
    public void test12() {
        client.put(0,1000);
        client.put(1,500);
        client.put(2,2000);
        client.put(3,1500);
        primaryServers[1].shutDown();
        fusedServers[1].shutDown();
        FusionHashMap<Integer, Integer>[] data = Fusion.recover(primaryHosts,fusedHosts,primaryPorts,fusedPorts);
        assertTrue(data[0].get(0) == 1000);
        assertTrue(data[0].get(2) == 2000);
        assertTrue(data[1].get(1) == 500);
        assertTrue(data[1].get(3) == 1500);
    }

    public static boolean execute(String input, Map<Integer, Integer> store) {
        List<String> ops = new ArrayList<>();
        ConfigReader.readOperations(input,ops);
        long start = Util.getCurrTime();
        for(int i = 0;i < ops.size(); i++) {
            StringTokenizer st = new StringTokenizer(ops.get(i));
            String arg = st.nextToken();
            int key = Integer.parseInt(st.nextToken());
            int val = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : -1;
            Status s = client.doOperation(arg, key, val,store);
            assertTrue(s == Status.OK);
        }
        Random rand = new Random();
        client.doOperation("down",null, rand.nextInt(numPrimaries), store);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FusionHashMap[] primaries = Fusion.recover(primaryHosts,fusedHosts,primaryPorts,fusedPorts);
        boolean ok = client.checkRecover(primaries,store);
        return ok;
    }
    @Test
    public void test13() {
        int i = 0;
        while(i < 20) {
            init();
            InputGenerator.generateInput(5, "input.json", 100);
            assertTrue(execute("input.json", new HashMap<Integer, Integer>()));
            end();
            i++;
        }
    }

    @Test
    public void test14() {
        int i = 0;
        while(i < 50) {
            init();
            InputGenerator.generateInput(20, "input.json", 100);
            assertTrue(execute("input.json", new HashMap<Integer, Integer>()));
            end();
            i++;
        }
    }

    @Test
    public void test15() {
        int i = 0;
        while (i < 50) {
            init();
            InputGenerator.generateInput(20, "input.json", -1000, 1000);
            assertTrue(execute("input.json", new HashMap<Integer, Integer>()));
            end();
            i++;
        }
    }
}
