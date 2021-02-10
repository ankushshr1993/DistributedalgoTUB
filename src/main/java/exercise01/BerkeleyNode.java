package exercise01;

import io.jbotsim.core.Message;
import io.jbotsim.core.Node;
import io.jbotsim.ui.JClock;
import util.DetectAndElectNode;
import util.MessageFlag;
import util.NodeStatus;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class BerkeleyNode extends DetectAndElectNode {
    // Timer
    private final static int TIMERRESOLUTION = 1;
    private final static int min = -1;
    private final static int max = 1;
    private final static double alpha = 0.3;
    private boolean syncCoordinator;
    private LocalClock timer;
    private Random rand = new Random();
    private Map<Node, List<Node>> pathMap;

    @Override
    public void onStart() {
        super.onStart();
        timer = new LocalClock(TIMERRESOLUTION, rand.nextDouble() * 0.1 - 0.05);
    }

    @Override
    public void onSelection() {
        super.onSelection();

        if (isWinner() || status == NodeStatus.TIME_SYNC) {
            timer.setClockSpeed(1.3);
            syncCoordinator = true;
            startTimeSync();
        }
    }

    @Override
    public void onClock() {
        timer.onClock();
//        log("Current Time: " + timer.getCurrentTime());
    }

    @Override
    public void onMessage(Message message) {
        super.onMessage(message);

        MessageFlag flag = MessageFlag.valueOf(message.getFlag());
        if (flag == MessageFlag.BERKELEY_GET_TIME) {
            BerkeleyContent content = (BerkeleyContent) message.getContent();
            List<Node> path = content.path;
            if (this.equals(content.path.get(path.size() - 1))) {
                // reach target node
//                log("Current Time: " + timer.getCurrentTime() + " get request with path " + path.toString());
                content.timeReply = timer.getCurrentTime();
                send(path.get(path.indexOf(this) - 1), new Message(content, MessageFlag.BERKELEY_TIME_VALUE.toString()));
            } else if (this.equals(content.path.get(0))) {
                // Sender receive information
                log("Error");
            } else {
                // path node, passing message
                send(path.get(path.indexOf(this) + 1), message);
            }
        } else if (flag == MessageFlag.BERKELEY_TIME_VALUE) {
            BerkeleyContent content = (BerkeleyContent) message.getContent();
            List<Node> path = content.path;

            if (this.equals(content.path.get(path.size() - 1))) {
                // reach target node
                log("Error");
            } else if (this.equals(content.path.get(0))) {
                // Sender receive information
                double rtt_2 = (double) (timer.getCurrentTime() - content.timeRequest) / 2;
                double estimate = content.timeReply + rtt_2;
//                log("get reply from: " + path.get(path.size() - 1).getID() +
//                        " Local time: " + timer.getCurrentTime() +
//                        " time replay: " + content.timeReply +
//                        " RTT: " + (timer.getCurrentTime() - content.timeRequest) +
//                        " estimate time of it is: " + estimate +
//                        " shift: " + (timer.getCurrentTime() - estimate));
                content.shift = timer.getCurrentTime() - estimate;
                content.timeRequest = timer.getCurrentTime();
                content.rtt_2 = rtt_2;
                send(path.get(path.indexOf(this) + 1), new Message(content, MessageFlag.BERKELEY_SET_TIME.toString()));
            } else {
                // path node, passing message
                send(path.get(path.indexOf(this) - 1), message);
            }
        } else if (flag == MessageFlag.BERKELEY_SET_TIME) {
            BerkeleyContent content = (BerkeleyContent) message.getContent();
            List<Node> path = content.path;

            if (this.equals(content.path.get(path.size() - 1))) {
                // reach target node
                log(" local time: " + timer.getCurrentTime() +
                        " get shift command: " + content.shift +
                        " speed of clock:" + timer.getClockSpeed());
                timer.adjustTime(content.shift);
                // If difference larger than threshold we send Time_Value back to sender
                if (Math.abs((content.timeRequest + content.rtt_2) - timer.getCurrentTime()) > alpha) {
                    content.timeReply = timer.getCurrentTime();
                    send(path.get(path.indexOf(this) - 1), new Message(content, MessageFlag.BERKELEY_TIME_VALUE.toString()));
                }

            } else if (this.equals(content.path.get(0))) {
                // Sender receive information
                log("Error");
            } else {
                // path node, passing message
                send(path.get(path.indexOf(this) + 1), message);
            }
        }
    }

    private void startTimeSync() {
        status = NodeStatus.TIME_SYNC;
        pathMap = detectionAgent.getGraph().getAllPath(this);
        for (Node n : pathMap.keySet()) {
            Message timePollMessage =
                    new Message(
                            new BerkeleyContent(timer.getCurrentTime(), pathMap.get(n)),
                            MessageFlag.BERKELEY_GET_TIME.toString());
            send(pathMap.get(n).get(1), timePollMessage);
        }
    }
}
