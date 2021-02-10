package exercise01;

import io.jbotsim.core.Topology;
import io.jbotsim.ui.JViewer;
import util.MessageCounter;
import util.TopologyFactory;

public class Ex01 {
    public static void main(String[] args){
        MessageCounter.resetCounters();

        final int NODES = 9;

        final int TOPOLOGY_TIME_UNIT = 100;

        String path = "";
        path = (args.length > 0 && args[0] != null && !"".equals(args[0])) ? args[0] : path;

        Topology topology;

        if (!"".equals(path)) {
            topology = TopologyFactory.readFromFilePath(path, BerkeleyNode::new);
        } else {
            topology = TopologyFactory.generateTopology(BerkeleyNode::new, TopologyFactory.TopologyType.RING, false, NODES);
        }
        topology.setTimeUnit(TOPOLOGY_TIME_UNIT);
        new JViewer(topology);
        topology.start();
    }
}
