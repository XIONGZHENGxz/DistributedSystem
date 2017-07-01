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
    private static final List<Integer> list = new ArrayList<>();

    public static void generateInput(int size, String outputFile,int max) {
        JSONArray ops = new JSONArray();
        int i = 0;
        while(i < size) {
            String cmd = cmds[rand.nextInt(3)];
            if(cmd.equals("put")) {
                int key = rand.nextInt(1000000);
                int val = rand.nextInt(1000000);
                list.add(key);
                ops.add(cmd+" "+key+" "+val);
                i++;
            } else {
                if(list.size() == 0) continue;
                int ind = rand.nextInt(list.size());
                int key = list.get(rand.nextInt(list.size()));
                ops.add(cmd+" "+key);
                if(cmd.equals("remove")) list.remove(ind);
                i++;
            }
        }
        ops.add("down "+ rand.nextInt(max));
        System.out.println(ops.size()+" "+ops.get(0));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Operations", ops);
        try {
            FileWriter fw = new FileWriter(outputFile);
            System.out.println(jsonObject.toJSONString());
            fw.write(jsonObject.toJSONString());
            fw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
