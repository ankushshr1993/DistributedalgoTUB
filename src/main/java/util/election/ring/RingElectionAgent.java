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

public class RingElectionAgent extends ElectionAgent {
    private final DetectAndElectNode node;

    private int myValue;
    private int maxnum;

    private Node left;
    private Node right;

    private final List<Message> replies = new ArrayList<>();

    public RingElectionAgent(DetectAndElectNode node) {
        this.node = node;
        onStart();
    }

    public void onStart() {
        this.node.setStatus(NodeStatus.ELECTION_RING_IDLE);
        this.myValue = this.node.getID();
        this.maxnum = 1;
        this.left = this.node.getOutNeighbors().get(0);
        this.right = this.node.getOutNeighbors().get(1);
        this.replies.clear();

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
        MessageFlag flag = MessageFlag.valueOf(message.getFlag());

        if (NodeStatus.ELECTION_RING_IDLE.equals(this.node.getStatus())) {
            sendPass(message);
            return;
        }

        switch (flag) {
            case ELECTION_RING_FROM:
                handleCandidature(message);
                break;
            case ELECTION_RING_OK:
            // case ELECTION_RING_NO:
                handleReply(message);
                break;
        }
    }

    private void handleCandidature(Message message) {
        RingElectionContent content = (RingElectionContent) message.getContent();
        int value = content.getValue();
        int num = content.getNum();
        int maxnum = content.getMaxnum();

        if (value < this.myValue) {
            // OPTIMIZATION: DO NOTHING ('discard token')
            // sendEcho(content, MessageFlag.ELECTION_RING_NO.toString());
        } else if (value > this.myValue) {
            loseElection();

            num++;
            content.setNum(num);
            if (num < maxnum) {
                sendPass(message);
            } else {
                sendEcho(content, MessageFlag.ELECTION_RING_OK.toString());
            }
        } else {
            winElection();
        }
    }

    private void handleReply(Message message) {
        RingElectionContent content = (RingElectionContent) message.getContent();
        int value = content.getValue();

        if (this.myValue == value) {
            // This is a reply for this node
             if (NodeStatus.ELECTION_RING_LOST.equals(this.node.getStatus())) {
                 return;
             }

            this.replies.add(message);

            if (this.replies.size() == 2) {
                // log("Received both replies.");
                log("Received two confirmations.");

                // *******************************
                // Comment out for optimization
                /**if (this.replies.stream().anyMatch(m -> MessageFlag.ELECTION_RING_NO.equals(m.getFlag()))) {
                    log("At least one of the replies is " + MessageFlag.ELECTION_RING_NO);
                    loseElection();
                }*/
                // *******************************

                this.replies.clear();
                this.maxnum *= 2;

                if (NodeStatus.ELECTION_RING_CANDIDATE.equals(this.node.getStatus())) {
                    Message newMessage = createCandidateMessage();
                    sendBoth(newMessage);
                }
            }
        } else {
            // This is a reply a different node is waiting for
            sendPass(message);
        }
    }

    private Message createCandidateMessage() {
        RingElectionContent content = new RingElectionContent(this.node.getID(), 0, this.maxnum);
        return new Message(content, MessageFlag.ELECTION_RING_FROM.toString());
    }

    private void sendBoth(Message message) {
        log("Sending a message in both directions with depth " +
                ((RingElectionContent) message.getContent()).getMaxnum() + ".");

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

    private void sendEcho(Object content, String flag) {
        Message message = new Message(content, flag);
        if (this.left.equals(message.getSender())) {
            sendLeft(message);
        } else {
            sendRight(message);
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
