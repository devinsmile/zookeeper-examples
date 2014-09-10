package uk.co.softsquare.zookeeper;

import com.google.common.collect.ImmutableMap;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * @author dan
 */
public class ZooConfig {

    private final File dataDir;
    private final int clientPort;
    private final List<ZooServer> servers;

    public ZooConfig(File dataDir, int clientPort, List<ZooServer> servers) {
        this.dataDir = dataDir;
        this.clientPort = clientPort;
        this.servers = servers;
    }

    public QuorumPeerConfig toQuorumPeerConfig() throws IOException, QuorumPeerConfig.ConfigException {
        QuorumPeerConfig config = new QuorumPeerConfig();
        Properties properties = new Properties();
        properties.putAll(ImmutableMap.builder()
                .put("tickTime", 2000)
                .put("initLimit", 1000)
                .put("syncLimit", 1000)
                .put("dataDir", dataDir.getAbsolutePath())
                .put("clientPort", clientPort)
                .build());
        for (ZooServer server : servers) {
            String[] serverString = server.asServerString().split("=");
            properties.put(serverString[0], serverString[1]);
        }
        config.parseProperties(properties);
        return config;
    }

    public int getClientPort() {
        return clientPort;
    }
}
