package util;

import util.detection.DetectionAgent;
import util.election.ElectionAgent;
import util.election.ring.RingElectionAgent;
import util.election.ring.RingElectionFranklinAgent;
import util.election.tree.TreeElectionAgent;
import io.jbotsim.core.Message;
import io.jbotsim.core.Node;

import java.util.ArrayList;
import java.util.List;

public class DetectAndElectNode extends Node {
    protected NodeStatus status = NodeStatus.DETECTION_IDLE;

    protected final DetectionAgent detectionAgent;
    protected ElectionAgent electionAgent;

    // Store any messages that may have been received before the ElectionAgent was set
    private final List<Message> messageBuffer = new ArrayList<>();

    public List<Message> getMessageBuffer() {
        return messageBuffer;
    }

    public DetectAndElectNode() {
        this.detectionAgent = new DetectionAgent(this);
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    @Override
    public void onStart() {
        MessageCounter.resetCounters();

        this.status = NodeStatus.DETECTION_IDLE;
        this.detectionAgent.onStart();
    }

    @Override
    public void onSelection() {
        switch (this.status) {
            case DETECTION_IDLE:
                log("I start the topology type detection.");
                this.detectionAgent.onSelection();
                break;
            case ELECTION_RING_IDLE:
                log("I will participate in the election.");
                this.electionAgent.onSelection();
                break;
            case ELECTION_TREE_IDLE:
                log("I will trigger the election.");
                this.electionAgent.onSelection();
                break;
        }
    }

    @Override
    public void onMessage(Message message) {
        MessageFlag flag = MessageFlag.valueOf(message.getFlag());

        switch (flag) {
            case DETECTION_EXPLORER:
            case DETECTION_ECHO:
            case DETECTION_INFORMATION:
                this.detectionAgent.onMessage(message);
                break;
            case ELECTION_RING_FROM:
            case ELECTION_RING_OK:
            case ELECTION_RING_NO:
            case ELECTION_TREE_EXPLOSION:
            case ELECTION_TREE_CONTRACTION:
            case ELECTION_TREE_INFORMATION:
                if (this.electionAgent == null) {
                    this.messageBuffer.add(message);
                } else {
                    this.electionAgent.onMessage(message);
                }
                break;
            default:
                // Unknown message. Do nothing.
                break;
        }
    }

    public void prepareElection(TopologyFactory.TopologyType topologyType) {
        if (TopologyFactory.TopologyType.RING.equals(topologyType)) {
            //this.electionAgent = new RingElectionAgent(this);
            this.electionAgent = new RingElectionAgent(this);
        } else if (TopologyFactory.TopologyType.TREE.equals(topologyType)) {
            this.electionAgent = new TreeElectionAgent(this);
        } else {
            log("Can't proceed with the election phase. Attempting to continue will produce unexpected errors.");
        }
    }

    public void sendMessage(Node destination, Message message) {
        MessageFlag flag = MessageFlag.valueOf(message.getFlag());

        switch (flag) {
            case DETECTION_EXPLORER:
            case DETECTION_ECHO:
            case DETECTION_INFORMATION:
                MessageCounter.increaseDetectionCount();
                break;
            case ELECTION_RING_FROM:
            case ELECTION_RING_OK:
            case ELECTION_RING_NO:
            case ELECTION_TREE_EXPLOSION:
            case ELECTION_TREE_CONTRACTION:
            case ELECTION_TREE_INFORMATION:
                MessageCounter.increaseElectionCount();
                break;
            default:
                MessageCounter.increaseUnknownCount();
                break;
        }

        send(destination, message);
    }

    public void sendAllNeighbors(Message message) {
        for (Node neighbor : getNeighbors()) {
            sendMessage(neighbor, message);
        }
    }

    public void sendAllExceptInformer(Node informer, Object content, String flag) {
        for (Node neighbor : getNeighbors()) {
            Message message = new Message(content, flag);
            if (!neighbor.equals(informer)) {
                sendMessage(neighbor, message);
            }
        }
    }

    public void log(String msg) {
        System.out.println("Node " + this.getID() + ": " + msg);
    }

    public boolean isWinner() {
        return this.status == NodeStatus.ELECTION_TREE_WON || this.status == NodeStatus.ELECTION_RING_WON;
    }
}