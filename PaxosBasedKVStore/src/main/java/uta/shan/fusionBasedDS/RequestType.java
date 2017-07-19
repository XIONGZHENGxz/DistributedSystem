package uta.shan.fusionBasedDS;

import java.io.Serializable;

/**
 * Created by xz on 7/17/17.
 */
public enum RequestType implements Serializable{
    GET("get"), REMOVE("rm"), PUT("put"), APPEND("app"),
    RECOVER("rec"), DOWN("down"), RESUME("resume");
    final static long serialVersionUID=1L;
    private String val;
    RequestType(String val) {
        this.val = val;
    }

    public String toString() {
        return val.toString();
    }
}
