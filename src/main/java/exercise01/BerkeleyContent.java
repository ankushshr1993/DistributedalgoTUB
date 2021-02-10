package exercise01;

import io.jbotsim.core.Node;

import java.util.List;
import java.util.Map;

public class BerkeleyContent {
    public double timeRequest;
    public double timeReply;
    public double shift;
    public double rtt_2;

    public final List<Node> path;

    public BerkeleyContent(double timeRequest, List<Node> path) {
        this.timeRequest = timeRequest;
        this.path = path;
    }
}
