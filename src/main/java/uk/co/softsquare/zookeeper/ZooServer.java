package uk.co.softsquare.zookeeper;

import com.google.common.base.Optional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZooServer {
    private static final Pattern PATTERN = Pattern.compile("^server\\.(\\d)=(\\w+):(\\d+):(\\d+)");
    final int index;
    final String host;
    final int peerConnectionPort;
    final int leaderElectionPort;

    public ZooServer(int index, String host, int peerConnectionPort, int leaderElectionPort) {
        this.index = index;
        this.host = host;
        this.peerConnectionPort = peerConnectionPort;
        this.leaderElectionPort = leaderElectionPort;
    }

    public String asServerString() {
        return String.format("server.%s=%s:%s:%s", index, host, peerConnectionPort, leaderElectionPort);
    }

    public static Optional<ZooServer> fromString(String string) {
        Matcher matcher = PATTERN.matcher(string);
        if (!matcher.find()) {
            return Optional.absent();
        } else {
            return Optional.of(new ZooServer(
                    Integer.parseInt(matcher.group(1)),
                    matcher.group(2),
                    Integer.parseInt(matcher.group(3)),
                    Integer.parseInt(matcher.group(4))));
        }
    }
}
