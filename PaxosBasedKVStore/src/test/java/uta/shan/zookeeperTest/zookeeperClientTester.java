package uta.shan.zookeeperTest;

/**
 * Created by xz on 7/12/17.
 */
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uta.shan.zookeeperBasedDS.Client;
import uta.shan.zookeeperBasedDS.DataType;

public class zookeeperClientTester {
    private static String LOCALHOST = "localhost";
    private static String[] hosts;

    @Before
    public void init() {
        hosts = new String[3];
        hosts[0] = "192.168.245.156";
        hosts[1] = "192.168.245.186";
        hosts[2] = "192.168.245.189";
    }

    @Test
    public void test1() {
        Client<Integer, String> client = new Client<>(hosts, DataType.STRING);
        assertTrue(client.put(10, "hello"));
        assertTrue(client.get(10).equals("hello"));
        assertTrue(client.remove(10));
        assertTrue(client.get(10) == null);
    }

    @Test
    public void test2() {
        Client<Integer, Integer> client = new Client<>(hosts, DataType.INTEGER);
        assertTrue(client.put(81345,746100));
        assertTrue(client.put(81345,74610));
        assertTrue(client.get(81345) == 74610);
        assertTrue(client.remove(81345));
        assertTrue(client.get(10) == null);
    }
}
