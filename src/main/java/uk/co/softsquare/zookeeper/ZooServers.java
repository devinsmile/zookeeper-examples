package uk.co.softsquare.zookeeper;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ZooServers {

    public static final List<ZooServer> QUORUM = ImmutableList.<ZooServer>builder()
            .add(ZooServer.fromString("server.0=localhost:2888:3888").get())
            .add(ZooServer.fromString("server.1=localhost:4888:5888").get())
            .add(ZooServer.fromString("server.2=localhost:6888:7888").get())
            .build();

    public static ZooConfig create(int index, File dataDir) {
        File myid = new File(dataDir, "myid");
        try (FileWriter writer = new FileWriter(myid)) {
            writer.write("" + index);
            writer.flush();
        } catch (IOException e) {
            Throwables.propagate(e);
        }
        return new ZooConfig(dataDir, 2100 + index, QUORUM);
    }

}
