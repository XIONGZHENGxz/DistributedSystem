package uta.shan.common;

import java.util.Map;

/**
 * Created by xz on 7/2/17.
 */
public class Comparator<K,V> {
    public boolean compare(Map<K,V> store1, Map<K,V> store2) {
        if(store1 == null && store2 == null) return true;
        if(store1 == null || store2 == null) return false;
        for(K key: store1.keySet()) {
            if(!store1.get(key).equals(store2.get(key))) return false;
        }
        return true;
    }
}
