package uta.shan.fusionBasedDS;

import java.io.Serializable;

/**
 * Created by xz on 7/17/17.
 */
public enum Status implements Serializable {
    OK("ok"),ERR("error"),NE("not exist");
    final static long serialVersionUID=1L;
    String val;
    Status(String val) {
        this.val = val;
    }
}
