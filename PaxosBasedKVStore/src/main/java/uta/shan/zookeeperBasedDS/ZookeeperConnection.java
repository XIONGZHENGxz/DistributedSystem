package uta.shan.zookeeperBasedDS;
/**
 * Created by xz on 7/12/17.
 */

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import uta.shan.communication.Util;

public class ZookeeperConnection {
    private ZooKeeper zk;
    private CountDownLatch latch;

    public ZookeeperConnection() {
        zk = null;
        latch = new CountDownLatch(1);
    }
    public void reset() {
        latch = new CountDownLatch(1);
    }

    public ZooKeeper connect(String host) throws IOException, InterruptedException {
        zk = new ZooKeeper(host, Util.ZOO_TIMEOUT, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if(event.getState() == Event.KeeperState.SyncConnected) {
                    latch.countDown();
                }
            }
        });
        latch.await();
        return zk;
    }

    public void close() throws InterruptedException {
        zk.close();
    }
}
