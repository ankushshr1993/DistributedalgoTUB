package util.election.ring;

import util.DetectAndElectNode;
import util.MessageFlag;
import util.NodeStatus;
import util.election.ElectionAgent;
import io.jbotsim.core.Color;
import io.jbotsim.core.Message;
import io.jbotsim.core.Node;
import util.MessageCounter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RingElectionFranklinAgent extends ElectionAgent {

    private final DetectAndElectNode node;

    private int myValue;

    private List<Integer> candidatures;

    private Node left;
    private Node right;

    public RingElectionFranklinAgent(DetectAndElectNode node) {
        this.node = node;
        onStart();
    }

    public void onStart() {
        this.node.setStatus(NodeStatus.ELECTION_RING_IDLE);
        this.myValue = this.node.getID();
        this.candidatures = new ArrayList<>();;
        this.left = this.node.getOutNeighbors().get(0);
        this.right = this.node.getOutNeighbors().get(1);

        /**
         * Check if any messages have been received before this node's ElectionAgent was set:
         * Such messages were stored in a messageBuffer in DetectAndElectNode.
         * Deal with those and then start the election too (unless this node has already lost
         * after dealing with the messages in the buffer)
         * */

        if ( !this.node.getMessageBuffer().isEmpty()) {

            for (Message msg : this.node.getMessageBuffer()) {

                onMessage(msg);

            }

        }

        /**
         * Start the election procedure with this node as the candidate, unless this node hasn't lost already
         * after dealing with messages that may have been kept in the messageBuffer
         * */

        if (NodeStatus.ELECTION_RING_CANDIDATE.equals(this.node.getStatus())) {

            onSelection();

        }
    }

    public void onSelection() {
        this.node.setStatus(NodeStatus.ELECTION_RING_CANDIDATE);
        this.node.setColor(Color.YELLOW);

        Message message = createCandidateMessage();
        sendBoth(message);
    }

    public void onMessage(Message message) {

        handleCandidature(message);

    }

    private void handleCandidature(Message message) {

        /**
         * If this node is still active (i.e., still a candidate participating in the election),
         * as soon as two candidatures (one from the left, one from the right) have been received,
         * compare them to the node's own ID and only continue if own ID is the maximum,
         * else become passive
         * */

        if (this.node.getStatus() == NodeStatus.ELECTION_RING_CANDIDATE) {

            RingElectionContent content = (RingElectionContent) message.getContent();
            int value = content.getValue();

            /** If own node ID is received, the node has won */

            if (value == this.myValue) {

                winElection();

            } else {

                /** Otherwise, add the value to the received candidatures and perform the comparison
                 * of candidatures if two candidatures have been received */

                this.candidatures.add(value);

                if (this.candidatures.size() == 2) {

                    this.candidatures.add(this.myValue);

                    Optional<Integer> maxValue = this.candidatures.stream()
                            .reduce((val1, val2)
                                    -> val1 > val2
                                    ? val1 : val2);

                    int maxID = maxValue.get();

                    if (maxID == this.myValue) {

                        Message contCandidature = createCandidateMessage();
                        sendBoth(contCandidature);

                        this.candidatures.clear();

                    } else {

                        loseElection();

                    }

                }
            }

            /** The node is already passive, pass the message on */

        } else {

            sendPass(message);

        }
    }

    private Message createCandidateMessage() {

        // NOTE: maxnum is always 1 with this algorithm

        RingElectionContent content = new RingElectionContent(this.node.getID(), 0, 1);
        return new Message(content, MessageFlag.ELECTION_RING_FROM.toString());
    }

    private void sendBoth(Message message) {
        log("Sending a message in both directions with depth " +
                ((RingElectionContent) message.getContent()).getMaxnum() + ".");
        log("Please note that depth is always 1 because of the logic of this algorithm!");

        sendLeft(message);

        // To avoid accessing the same content information using the reference, create a clone
        RingElectionContent duplicatedContent = new RingElectionContent((RingElectionContent) message.getContent());
        Message duplicatedMessage = new Message(duplicatedContent, message.getFlag());
        sendRight(duplicatedMessage);
    }

    private void sendLeft(Message message) {
        this.node.sendMessage(this.left, message);
    }

    private void sendRight(Message message) {
        this.node.sendMessage(this.right, message);
    }

    private void sendPass(Message message) {
        if (this.left.equals(message.getSender())) {
            sendRight(message);
        } else {
            sendLeft(message);
        }
    }

    private void winElection() {
        if (NodeStatus.ELECTION_RING_WON.equals(this.node.getStatus())) {
            return;
        }

        this.node.setStatus(NodeStatus.ELECTION_RING_WON);
        this.node.setColor(Color.WHITE);
        log("I won the election.");

        MessageCounter.printDetectionAndElectionMessages();
    }

    private void loseElection() {
        if (NodeStatus.ELECTION_RING_LOST.equals(this.node.getStatus())) {
            return;
        }

        this.node.setStatus(NodeStatus.ELECTION_RING_LOST);
        this.node.setColor(Color.BLACK);
        log("I lost the election.");
    }

    private void log (String msg) {
        this.node.log(msg);
    }
}

