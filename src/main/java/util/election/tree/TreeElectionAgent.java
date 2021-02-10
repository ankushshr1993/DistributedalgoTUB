package util.election.tree;

import util.DetectAndElectNode;
import util.MessageFlag;
import util.NodeStatus;
import util.election.ElectionAgent;
import io.jbotsim.core.Color;
import io.jbotsim.core.Message;
import io.jbotsim.core.Node;
import util.MessageCounter;

import java.util.List;

public class TreeElectionAgent extends ElectionAgent {
    private final DetectAndElectNode node;

    private List<Node> contractors;
    private int myValue;
    private int value;

    public TreeElectionAgent(DetectAndElectNode node) {
        this.node = node;
        onStart();
    }

    public void onStart() {
        this.node.setStatus(NodeStatus.ELECTION_TREE_IDLE);
        this.contractors = this.node.getNeighbors();
        this.myValue = this.node.getID();
        this.value = this.node.getID();
    }

    public void onSelection() {
        this.node.setStatus(NodeStatus.ELECTION_TREE_EXPLODED);
        this.node.setColor(Color.YELLOW);

        Message message = new Message(null, MessageFlag.ELECTION_TREE_EXPLOSION.toString());
        this.node.sendAllNeighbors(message);

        // Leaves that are initiators send their contraction message with their ID
        // immediately after the explosion message.
        if (this.node.getNeighbors().size() == 1) {
            contract();
        }
    }

    public void onMessage(Message message) {
        MessageFlag flag = MessageFlag.valueOf(message.getFlag());

        switch (flag) {
            case ELECTION_TREE_EXPLOSION:
                explode(message);
                break;
            case ELECTION_TREE_CONTRACTION:
                handleContraction(message);
                break;
            case ELECTION_TREE_INFORMATION:
                inform(message);
                break;
        }
    }

    private void explode(Message message) {
        switch (this.node.getStatus()) {
            case ELECTION_TREE_EXPLODED:
            case ELECTION_TREE_CONTRACTED:
                return;
        }

        this.node.setColor(Color.YELLOW);

        if (this.node.getNeighbors().size() > 1) {
            this.node.sendAllExceptInformer(message.getSender(), null, MessageFlag.ELECTION_TREE_EXPLOSION.toString());
        } else {
            // If a leaf receives an explosion message, send a contraction message.
            contract();
        }
    }

    private void contract() {
        this.node.setStatus(NodeStatus.ELECTION_TREE_CONTRACTED);
        this.node.setColor(Color.ORANGE);

        Message message = new Message(this.value, MessageFlag.ELECTION_TREE_CONTRACTION.toString());
        this.node.sendAllNeighbors(message);
    }

    private void handleContraction(Message message) {
        if (!NodeStatus.ELECTION_TREE_CONTRACTED.equals(this.node.getStatus())) {
            this.contractors.remove(message.getSender());

            int value = (int) message.getContent();
            if (value > this.value) {
                this.value = value;
            }

            if (this.contractors.size() == 1) {
                this.node.setColor(Color.ORANGE);
                this.node.setStatus(NodeStatus.ELECTION_TREE_CONTRACTED);
                Message contraction = new Message(this.value, MessageFlag.ELECTION_TREE_CONTRACTION.toString());
                this.node.sendMessage(this.contractors.get(0), contraction);
            }
        } else {
            // If I receive a contraction message when I already received neighbors-1 contraction messages
            // and I sent my contraction message, it means this is the edge where 2 contraction messages meet.
            log("I'm one of the nodes in the last edge.");
            this.value = Math.max(this.value, (int) message.getContent());
            this.node.setStatus(NodeStatus.ELECTION_TREE_INFORMED);
            if (this.myValue == this.value) {
                winElection();
            } else {
                this.node.setColor(Color.BLACK);
            }
            this.node.sendAllExceptInformer(message.getSender(), this.value,
                    MessageFlag.ELECTION_TREE_INFORMATION.toString());
        }
    }

    private void inform(Message message) {
        if (NodeStatus.ELECTION_TREE_INFORMED.equals(this.node.getStatus())) {
            return;
        }

        this.node.setStatus(NodeStatus.ELECTION_TREE_INFORMED);
        this.node.setColor(Color.GRAY);
        this.value = (int) message.getContent();

        if (this.myValue == this.value) {
            winElection();
        }

        this.node.sendAllExceptInformer(message.getSender(), this.value, MessageFlag.ELECTION_TREE_INFORMATION.toString());
    }

    private void winElection() {
        this.node.setStatus(NodeStatus.ELECTION_TREE_WON);
        this.node.setColor(Color.WHITE);
        log("I'm the leader.");
        MessageCounter.printDetectionAndElectionMessages();
    }

    private void log (String msg) {
        this.node.log(msg);
    }
}
