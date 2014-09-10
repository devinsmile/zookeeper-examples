package uk.co.softsquare.zookeeper;

import com.google.common.base.Optional;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ZooServerTest {
    @Test
    public void testString() throws Exception {
        Optional<ZooServer> serverOptional = ZooServer.fromString("server.1=zoo1:2888:3888");
        assertThat(serverOptional.isPresent(), is(true));
        assertThat(serverOptional.get().host, is("zoo1"));
        assertThat(serverOptional.get().index, is(1));
        assertThat(serverOptional.get().peerConnectionPort, is(2888));
        assertThat(serverOptional.get().leaderElectionPort, is(3888));
        assertThat(serverOptional.get().asServerString(), is("server.1=zoo1:2888:3888"));
    }
}