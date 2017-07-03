package uta.shan.config;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by xz on 6/11/17.
 */

public class ConfigReader {

    //read inputs
    public static void readOperations(String filePath, List<String> ops) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(filePath));
            JSONArray operations = (JSONArray) jsonObject.get("Operations");
            copyTo(operations,ops);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

    }
    //read number of primary and number of backups
    public static void readNumbers(String filePath, int[] nums) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(filePath));
            nums[0] = Integer.parseInt((String) jsonObject.get("number of primary"));
            nums[1] = Integer.parseInt((String) jsonObject.get("number of backup"));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

    }

    public static void readNumGroups(String filePath, int[] nums) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(filePath));
            nums[0] = Integer.parseInt((String) jsonObject.get("number of group"));
            nums[1] = Integer.parseInt((String) jsonObject.get("number of servers"));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public static void readJson(String filePath,String[] primaryHosts,String[] backupHosts,int[] primaryPorts, int[] backupPorts) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(filePath));
            JSONArray primaryServers = (JSONArray) jsonObject.get("primary hosts");
            JSONArray backupServers = (JSONArray) jsonObject.get("backup hosts");
            copyTo(primaryServers,primaryHosts);
            copyTo(backupServers,backupHosts);
            JSONArray pPorts = (JSONArray) jsonObject.get("primary ports");
            JSONArray bPorts = (JSONArray) jsonObject.get("backup ports");
            if(pPorts.size() == 1) {
                String pport = (String) pPorts.iterator().next();
                String bport = (String) bPorts.iterator().next();
                replicate(Integer.parseInt(pport),primaryPorts);
                replicate(Integer.parseInt(bport),backupPorts);
            } else {
                copyTo(pPorts, primaryPorts);
                copyTo(bPorts, backupPorts);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public static void readServers(String filePath,String[][] servers,int[][] ports) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(filePath));
            for (int i = 0; i < servers.length; i++) {
                JSONArray primaryServers = (JSONArray) jsonObject.get("group " + i + " primary hosts");
                copyTo(primaryServers, servers[i]);
                JSONArray pPorts = (JSONArray) jsonObject.get("group " + i + " primary ports");
                if (pPorts.size() == 1) {
                    String pport = (String) pPorts.iterator().next();
                    replicate(Integer.parseInt(pport), ports[i]);
                } else {
                    copyTo(pPorts, ports[i]);
                }
            }
        } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
    }

    public static void replicate(int port, int[] ports) {
        int n = ports.length;

        for(int i=0;i<n;i++) {
            ports[i] = port;
        }
    }

    public static void copyTo(JSONArray array, String[] list) {
        Iterator<String> iterator = array.iterator();
        for(int i=0;i<list.length;i++) {
            list[i] = iterator.next();
        }
    }

    public static void copyTo(JSONArray array, List<String> list) {
        Iterator<String> iterator = array.iterator();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
    }

    public static void copyTo(JSONArray array, int[] list) {
        Iterator<String> iterator = array.iterator();
        int ind = 0;
        while(iterator.hasNext()) {
            list[ind++] = Integer.parseInt(iterator.next());
        }
    }
}
