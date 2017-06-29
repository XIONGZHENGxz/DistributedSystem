package uta.shan.configTest;

/**
 * Created by xz on 6/12/17.
 */
import static org.junit.Assert.*;
import org.junit.Test;
import uta.shan.config.ConfigReader;

import java.util.ArrayList;
import java.util.List;

public class ConfidReaderTester {
    private static final String filePath = "configuration.json";
    private static final String inputPath = "input.json";

    @Test
    public void test1() {
        int[] nums = new int[2];
        ConfigReader.readNumbers(filePath,nums);
        String[] primaryHosts = new String[nums[0]] ;
        String[] backupHosts = new String[nums[1]];
        int[] primaryPorts = new int[nums[0]];
        int[] backupPorts = new int[nums[1]];

        ConfigReader.readJson(filePath,primaryHosts,backupHosts,primaryPorts,backupPorts);
        assertTrue(primaryHosts.length == 3);
        assertTrue(primaryHosts[0].equals("kamek.ece.utexas.edu"));
        assertTrue(primaryHosts[1].equals("koopa.ece.utexas.edu"));
        assertTrue(backupHosts.length == 2);
        assertTrue(backupHosts[0].equals("wario.ece.utexas.edu"));
        assertTrue(primaryPorts[0] == 5555);
        assertTrue(backupPorts[0] == 6666);
        assertTrue(primaryPorts.length == 3);
        assertTrue(backupPorts.length == 2);
    }

    @Test
    public void test2() {
        List<String> ops = new ArrayList<>();
        ConfigReader.readOperations(inputPath,ops);
        assertTrue(ops.get(0).equals("put 0 a"));
        assertTrue(ops.get(ops.size()-1).equals("put 7 abc"));
    }
}
