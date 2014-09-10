package uk.co.softsquare.zookeeper.client;

import com.google.common.base.Throwables;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.softsquare.zookeeper.ZooConfig;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class ServiceListing {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceListing.class);
    private static final String SERVICE_GROUPING = "/myservices";
    private final ZooKeeper zooKeeper;
    private final ConcurrentSkipListSet<String> knownServices = new ConcurrentSkipListSet<>();
    private final ZooConfig config;

    public ServiceListing(ZooConfig config) {
        this.config = config;
        this.zooKeeper = createZooKeeper(config);
    }

    private ZooKeeper createZooKeeper(ZooConfig config) {
        try {
            return new ZooKeeper("localhost:" + config.getClientPort(), 30000, new OnConnectionEstablished());
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void createServiceNode() {
        try {
            String newServiceName = "service" + config.getClientPort();
            zooKeeper.create(SERVICE_GROUPING + "/" + newServiceName, new byte[0],
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            knownServices.add(newServiceName);
            LOGGER.info("created service node '{}'", newServiceName);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    private void watchServices() {
        try {
            zooKeeper.getChildren(SERVICE_GROUPING, new ServicesUpdatedWatcher());
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    private List<String> getChildren() {
        try {
            return zooKeeper.getChildren(SERVICE_GROUPING, false);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void createRootNode() {
        try {
            zooKeeper.create(SERVICE_GROUPING, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            LOGGER.info("created root node");
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    private class OnConnectionEstablished implements Watcher {
        @Override
        public void process(WatchedEvent watchedEvent) {
            if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                // now connected
                LOGGER.info("ZooKeeper now connected!");
                zooKeeper.exists(SERVICE_GROUPING, false, new AsyncCallback.StatCallback() {
                    @Override
                    public void processResult(int rc, String s, Object o, Stat stat) {
                        KeeperException.Code code = KeeperException.Code.get(rc);
                        switch (code) {
                            case NONODE:
                                createRootNode();
                                watchServices();
                                createServiceNode();
                                break;
                            case NODEEXISTS:
                            case OK:
                                watchServices();
                                createServiceNode();
                                break;
                            default:
                                break;
                        }
                    }
                }, null);
            }
        }
    }

    private class ServicesUpdatedWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            Event.EventType type = event.getType();
            switch (type) {
                case NodeChildrenChanged:
                    knownServices.clear();
                    knownServices.addAll(getChildren());
                    LOGGER.info("now know of {}", knownServices);
                    break;
                case NodeCreated:
                case NodeDataChanged:
                case NodeDeleted:
                case None:
                    break;
            }
            watchServices();
        }
    }
}
