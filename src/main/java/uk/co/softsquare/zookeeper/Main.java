package uk.co.softsquare.zookeeper;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.apache.zookeeper.server.quorum.QuorumPeerMain;
import uk.co.softsquare.zookeeper.client.ServiceListing;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        int index = Integer.parseInt(args[0]);
        File datadir = new File("datadir" + index);
        datadir.mkdirs();
        ZooConfig zooConfig = ZooServers.create(index, datadir);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new QuorumPeerMain().runFromConfig(zooConfig.toQuorumPeerConfig());
                } catch (Exception e) {
                    Throwables.propagate(e);
                }
            }
        }).start();

        new ServiceListing(zooConfig);

        Thread.currentThread().join();
    }
}
