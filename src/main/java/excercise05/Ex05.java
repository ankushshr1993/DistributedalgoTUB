package exercise05;

import excercise05.Sk;
import io.jbotsim.core.Topology;
import io.jbotsim.ui.JViewer;
import util.MessageCounter;
import util.TopologyFactory;

public class Ex05 {
    public static final int NODES = 6 ;

    public static void main(String[] args){
        MessageCounter.resetCounters();



        final int TOPOLOGY_TIME_UNIT = 100;
        String path = "";
        path = (args.length > 0 && args[0] != null && !"".equals(args[0])) ? args[0] : path;

        Topology topology;

        if (!"".equals(path)) {
            topology = TopologyFactory.readFromFilePath(path, Sk::new);
        } else {
            topology = TopologyFactory.generateTopology(Sk::new, TopologyFactory.TopologyType.COMPLETE, false, NODES);
        }
        topology.setTimeUnit(TOPOLOGY_TIME_UNIT);
        new JViewer(topology);
        topology.start();

    }
}

/*
        int[] TokenQ = new int[this.RING_NODES];
        int[] LN = new int[this.RING_NODES];
        int me;                // ID of this node.
        int state;             // Current state of this node.
        boolean[] needToSendReq = new boolean[RING_NODES];
        int[] RN = new int[RING_NODES];
    }
        @Override
    public void onSelection() {
        for (int i = 0; i < this.RING_NODES; i++) {
            RN[i] = 0;
            LN[i] = 0;
            TokenQ[i] = 5;
            seqNum = 0;
        }
        if (me==0) {
            state = enterCSState;
            gotToken = true;
        } else {
            state = requestCSState;
            gotToken = false;
        }
    }

    @Override
    public void onSelection() {
        // JBotSim executes this method on a selected node
    }

    @Override
    public void onClock() {
        // JBotSim executes this method on each node in each round
    }

    @Override
    public void onMessage(Message message) {
        // JBotSim executes this method on a node every time it receives a message
    }


    }

// Find number of nodes
// Create array RN{1:N} for each node and
move the value of RN(J) to LN{J} after entering CS
//A queue Q This data structure is used by the token to keep record of ID of sites waiting for the token


//Select node to enter critical section
Onselection for LN[i]
Sn=Sn+1
case requestCSState:
               if (!gotToken) {
                   seqNum++;
                   RN[me]=seqNum;
                   for (int i=0;i<Ex05.NODES;i++)
                       if (i!=me)
                           needToSendReq[i]=true;
                       changeState(sendRequestToState);
                       return true;

case sendRequestToState:
                if (any(needToSendReq) {
                    changeState(toState);
                    return true;
                } else if (b.equals(SENDREQUEST)) {
                    changeState(waitingState);
                    return true;
                }
                return false;
send request REQUEST(i, sn,message) to all

public void onMessage(Message message)
 message =REQUEST(i, sn)
 RNj[i] = max(RNj[i], sn)
 RNj[i] = LN[i] + 1

// executeCS{check if Si acquired token if it has then
// print "entered critical section +delay 100ms"}

//ReleaseCS{
// if executeCS complete
//LN[i] = RNi[i]
//if RNi[j] = LN[j] + 1 then Append id to Q

//if q is empty keep token
//else pop ID from q and sendtoken()
// }

*/
