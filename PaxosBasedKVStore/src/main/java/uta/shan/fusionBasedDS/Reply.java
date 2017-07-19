package uta.shan.fusionBasedDS;

import java.io.Serializable;

/**
 * Created by xz on 7/17/17.
 */
public class Reply<E> implements Serializable {
    final static long serialVersionUID=1L;
    private E val;
    private Status status;

    public Reply(){}

    public Reply(Status status) {
        this(null, status);
    }
    public Reply(E val, Status status) {
        this.val = val;
        this.status = status;
    }

    public E getVal() {
        return this.val;
    }

    public Status getStatus() {
        return status;
    }

    public void setVal(E val) {
        this.val = val;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
