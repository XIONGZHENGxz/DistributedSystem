package uta.shan.common;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by xz on 6/30/17.
 */
public class InputGenerator {
    private static final Random rand = new Random();
    private static final String[] cmds = {"get", "put", "remove"};
    private static final List<String> strings = new ArrayList<>();

    public static void generateInput(int size, String outputFile,int max) {
        List<Integer> list = new ArrayList<>();
        JSONArray ops = new JSONArray();
        int i = 0;
        while(i < size) {
            String cmd = "";
            if(i % 10 == 9) cmd = "remove";
            else if(i % 10 == 8) cmd = "get";
            else cmd = "put";

            if(cmd.equals("put")) {
                int key = rand.nextInt(max);
                int val = rand.nextInt(max);
                list.add(key);
                ops.add(cmd+" "+key+" "+val);
                i++;
            } else {
                if(list.size() == 0) continue;
                int ind = rand.nextInt(list.size());
                int key = list.get(ind);
                ops.add(cmd+" "+key);
                if(cmd.equals("remove")) list.remove(ind);
                i++;
            }
//            System.out.println("cmd:: "+cmd);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Operations", ops);
        try {
            FileWriter fw = new FileWriter(outputFile);
            fw.write(jsonObject.toJSONString());
            fw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void generateInput(int size, String outputFile,int min, int max) {
        List<Integer> list = new ArrayList<>();
        JSONArray ops = new JSONArray();
        int i = 0;
        while(i < size) {
            String cmd = "";
            if(i % 10 == 9) cmd = "remove";
            else if(i % 10 == 8) cmd = "get";
            else cmd = "put";

            if(cmd.equals("put")) {
                int key = min + rand.nextInt(max - min + 1);
                int val = min + rand.nextInt(max - min + 1);
                list.add(key);
                ops.add(cmd+" "+key+" "+val);
                i++;
            } else {
                if(list.size() == 0) continue;
                int ind = rand.nextInt(list.size());
                int key = list.get(ind);
                ops.add(cmd+" "+key);
                if(cmd.equals("remove")) list.remove(ind);
                i++;
            }
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Operations", ops);
        try {
            FileWriter fw = new FileWriter(outputFile);
            fw.write(jsonObject.toJSONString());
            fw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generateInput(int keyLen, int valLen, int size, String outputFile) {
        JSONArray ops = new JSONArray();
        int i = 0;
        while(i < size) {
            String cmd = cmds[rand.nextInt(3)];
            if(cmd.equals("put")) {
                String key = randomString(keyLen);
                String val = randomString(valLen);
                strings.add(key);
                ops.add(cmd+" "+key+" "+val);
                i++;
            } else {
                if(strings.size() == 0) continue;
                int ind = rand.nextInt(strings.size());
                String key = strings.get(ind);
                ops.add(cmd+" "+key);
                if(cmd.equals("remove")) strings.remove(ind);
                i++;
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Operations", ops);
        try {
            FileWriter fw = new FileWriter(outputFile);
            fw.write(jsonObject.toJSONString());
            fw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < len; i++) {
            sb.append('0' + rand.nextInt(128));
        }

        return sb.toString();
    }


    public static void main(String...args) {
        int size = Integer.parseInt(args[0]);
        String file = args[1];
        int min = Integer.parseInt(args[2]);
        int max = Integer.parseInt(args[3]);
        generateInput(size, file, min, max);
    }
}
