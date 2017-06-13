package uta.shan.fusionBasedDSTest;

/**
 * Created by xz on 6/12/17.
 */
import static org.junit.Assert.*;
import org.junit.Test;
import uta.shan.communication.Messager;
import uta.shan.fusionBasedDS.PrimaryServer;

public class SimpleTest {
    @Test
    public void test1() {
//        String[] hosts =
//        PrimaryServer primaryServer = new PrimaryServer(null,null,0,5555);
        String reply = Messager.sendAndWaitReply("get 0","146.6.221.76",5555);
        System.out.println(reply);
    }
}
