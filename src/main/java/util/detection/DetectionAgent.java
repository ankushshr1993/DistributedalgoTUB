package util.detection;

import util.TopologyFactory;
import util.DetectAndElectNode;
import util.MessageFlag;
import util.NodeStatus;
import io.jbotsim.core.Color;
import io.jbotsim.core.Message;
import io.jbotsim.core.Node;

public class DetectionAgent {
    private final DetectAndElectNode node;

    private boolean informed = false;
    private boolean initiator = false;

    private Node activationEdge = null;
    private int numberOfMessages = 0;

    private Graph graph = new Graph();
    private TopologyFactory.TopologyType topologyType;

    public TopologyFactory.TopologyType getTopologyType() {
        return topologyType;
    }

    public DetectionAgent(DetectAndElectNode node) {
        this.node = node;
    }

    public void onStart() {
        this.node.setColor(Color.WHITE);

        this.informed = false;
        this.initiator = false;
        this.activationEdge = null;
        this.numberOfMessages = 0;
        this.topologyType = null;

        this.graph = new Graph();
        this.graph.getNodes().addAll(this.node.getNeighbors());
        this.graph.getLinks().addAll(this.node.getLinks());
    }

    public void onSelection() {
        this.node.setColor(Color.MAGENTA);
        this.node.setStatus(NodeStatus.DETECTION_EXPLORED);

        this.initiator = true;

        Message message = new Message(null, MessageFlag.DETECTION_EXPLORER.toString());
        this.node.sendAllNeighbors(message);

    }

    public void onMessage(Message message) {
        MessageFlag flag = MessageFlag.valueOf(message.getFlag());

        switch (flag) {
            case DETECTION_EXPLORER:
                explore(message);
                tryEchoBack();
                break;
            case DETECTION_ECHO:
                echo(message);
                tryEchoBack();
                break;
            case DETECTION_INFORMATION:
                inform(message);
                this.node.prepareElection(getTopologyType());
                break;
        }
    }

    private void explore(Message message) {
        if (!informed && MessageFlag.DETECTION_EXPLORER.equals(message.getFlag())) {
            this.activationEdge = message.getSender();
            this.informed = true;
            this.node.setColor(Color.RED);

            if (this.node.getNeighbors().size() > 1) {
                // Node is not a leaf. It sends explorers to all its neighbors except the activation edge.
                this.node.sendAllExceptInformer(this.activationEdge, null, MessageFlag.DETECTION_EXPLORER.toString());
            }
        }

        this.numberOfMessages++;
    }

    private void echo(Message message) {
        // If an echo message is received, store the information that it carries.
        Graph receivedGraph = (Graph) message.getContent();
        this.graph.getNodes().addAll(receivedGraph.getNodes());
        this.graph.getLinks().addAll(receivedGraph.getLinks());

        this.numberOfMessages++;
    }

    private void tryEchoBack() {
        if (numberOfMessages == this.node.getInNeighbors().size()) {
            this.node.setColor(Color.YELLOW);

            if (!initiator) {
                this.node.setStatus(NodeStatus.DETECTION_CONSULTED);
                // A Node that has received messages from all its edges becomes green
                // and sends an echo over its activation edge.
                // The initiator can't perform this task because it has no activation edge.

                Message echo = new Message(this.graph, MessageFlag.DETECTION_ECHO.toString());
                this.node.sendMessage(this.activationEdge, echo);
            } else {
                log("The initiator has received messages from all its neighbors.");
                log("The information gathered from the echo messages is:");
                log(this.graph.toString());

                this.topologyType = detectTopology(this.graph);
                this.node.prepareElection(this.topologyType);
                this.node.setColor(Color.CYAN);

                // Inform all nodes about the topology type
                log("Informing all nodes about the topology type.");
                Message topologyType = new Message(this.topologyType, MessageFlag.DETECTION_INFORMATION.toString());
                this.node.sendAllNeighbors(topologyType);

                log("Don't forget to pause the execution to proceed with the selection of nodes that " +
                        "will participate in the election.");
            }
        }
    }

    private void inform(Message message) {
        if (this.topologyType == null) {
            this.topologyType = (TopologyFactory.TopologyType) message.getContent();

            this.node.setColor(Color.BLUE);
            this.node.setStatus(NodeStatus.DETECTION_INFORMED);
            this.node.sendAllExceptInformer(message.getSender(), message.getContent(), message.getFlag());
        }
    }

    private TopologyFactory.TopologyType detectTopology(Graph graph) {
        if (graph.isTree()) {
            log("The topology is a Tree.");
            return TopologyFactory.TopologyType.TREE;
        } else if (graph.isRing()) {
            log("The topology is a Ring.");
            return TopologyFactory.TopologyType.RING;
        } else {
            log("The topology is neither a Tree nor a Ring.");
            return TopologyFactory.TopologyType.RANDOM;
        }
    }

    private void log (String msg) {
        this.node.log(msg);
    }

    public Graph getGraph() {
        return graph;
    }
}
