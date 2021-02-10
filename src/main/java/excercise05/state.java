package excercise05;

import exercise05.Ex05;
import io.jbotsim.core.Color;
import io.jbotsim.core.Message;
import io.jbotsim.core.Node;

import java.util.List;


public class state extends Node {

    private static final String
            REQUEST = "CS",
            SENDREQUEST = "Request",
            WAITING = "Token",
            ENTER = "CS",
            IN = " You are in the critical section. Leave ",
            LEAVE = "Now",
            SENDTOKEN = "Token";

    // Data structure for messages
    public int[] TokenQ = new int[Ex05.NODES];
    public int[] LN = new int[Ex05.NODES];

    private boolean[] needToSendReq = new boolean[Ex05.NODES];
    private int[] RN = new int[Ex05.NODES];
    private boolean gotToken = false;
    private int seqNum = 0;
    private boolean inQ = false;
    private int[] TempQ = new int[Ex05.NODES];
    private int next = 0;
    private List otherNodes;

    // State machine
    private int toNode;
    private static final Message tokenMSG = new Message(1);
    private static final Message requestMSG = new Message(2);
    private static final Message TokenQMSG = new Message(3);
    private static final Message LNMSG = new Message(4);
    //private int state = requestCSState;
    static final int requestCSState = 0;
    static final int sendRequestToState = 1;
    static final int toState = 2;
    static final int waitingState = 3;
    static final int enterCSState = 4;
    static final int inCSState = 5;
    static final int sendTokenState = 6;


    int me = 0;
    boolean informed;
    boolean initiator;
    boolean CsState;

    final String Check = "Critical Section";
    String Confirmation = "Confirmation";
    Node explorerSender = new Node();
    Node A = new Node();
    int i = 0;
    private int state = requestCSState;

    public void init() {
        me = this.getID();
        RN[i] = 0;
        LN[i] = 0;
        TokenQ[i] = 5;
        seqNum = 0;
        setColor(Color.RED);
        if (me == 5) {
            gotToken = true;
            state = enterCSState;
        }
        // System.out.println(me );
    }

    public void test() {

        setColor(Color.yellow);
        me = this.getID();
     /*   if (gotToken) {
            state = enterCSState;
        } else {
            state = requestCSState;
        }        */
        //seqNum++;
        RN[me] = seqNum++;
        Message message= new Message(seqNum);
        sendAll(message);

        System.out.println("message sent to: "+this.getOutbox());

       // stateMachine(state);


    }

    public void onMessage(Message message) {
        for(int i =0;i<Ex05.NODES;i++) {
            if (informed) {
                System.out.println("Msg received :"+message + "Node ID" + i);
            }
        }}}
        /*
    protected boolean stateMachine(int state) {
         }   switch (state) {
            case requestCSState:
                System.out.println("Request CS State " + this.getID() + state);
                seqNum++;
                RN[me] = seqNum;
               // System.out.println("Sequence " + seqNum);
                for (int i = 0; i < Ex05.NODES; i++)
                    if (i != me)
                       needToSendReq[i] = true;

                changeState(toState);

            case sendRequestToState:
                if (any(needToSendReq)) {
                    System.out.println("SendRequest To State " + this.getID() + state);
                    changeState(toState);
                } else { System.out.println("Waiting state est To State " + this.getID() + state);
                    changeState(waitingState);
                }
                return true;

            case toState:
                if (!any(needToSendReq)) {
                    System.out.println(" To State " + this.getID() + state);
                    if (!gotToken) {
                        changeState(waitingState);
                        System.out.println(" Nahi Hai token");
                    } else {
                        changeState(inCSState);
                        System.out.println("Hai token");
                    }
                } else {
                    for (int i = 0; i < this.getNeighbors().size(); i++) {
                        sendMessage(requestMSG, this.getNeighbors().get(i).getID(), me, seqNum);
                        System.out.println(me + " Sending message to Node ID...." + this.getNeighbors().get(i).getID());
                    }
             /*       for (int i =0;i<Ex05.NODES;i++)
                  {sendMessage(TokenQMSG, this.getNeighbors().get(i).getID(), i, TokenQ[i]); }
                   for (int i =0;i< Ex05.NODES;i++)
                   {sendMessage(LNMSG,this.getNeighbors().get(i).getID(), i, LN[i]);}
                   changeState(sendRequestToState);    }   */

     /*
                return true;

            case waitingState:
                if (gotToken) {
                    System.out.println("waiting state " + this.getID() + state);
                    changeState(inCSState);
                } else {
                    return false;
                }

            case enterCSState:
                System.out.println("CS State entered" + this.getID() + state);
                if (gotToken) {
                    changeState(inCSState);
                } else {
                    changeState(requestCSState);
                }
                return true;

            case inCSState:
                LN[me] = RN[me];
                for (int j = 0; j < Ex05.NODES; j++)
                    if (RN[j] == (LN[j] + 1)) {
                        appendQ(j);
                        System.out.println("IN CS State entered" + this.getID() + state);
                        changeState(enterCSState);
                    } else {
                        changeState(sendTokenState);
                        return true;
                    }
            case sendTokenState:
                getNextSite();
                System.out.println("Get next site" + next);
                sendMessage(tokenMSG, next, 0, 0);
                for (int i = 0; i < Ex05.NODES; i++)
                    sendMessage(TokenQMSG, next, i, TokenQ[i]);
                for (int i = 0; i < Ex05.NODES; i++)
                    sendMessage(LNMSG, next, i, LN[i]);
                gotToken = false;
                changeState(requestCSState);

            default:
                return false;
        }
    }
//send message

    /*
     // Alternative form of the receive method which carries an object as the messgae
     protected void receive(int message, int parm1, Object parm2){}
     // Send a message to another nodes.
     protected void send(int message, int to, int parm1, Object parm2) {
     ((DistAlg) da[to]).receive(message, parm1, parm2);
   }

         */
  /*  protected void changeState(int newState) {
        state = newState;
        stateMachine(state);

    }

    private boolean any(boolean[] a) {
        for (int i = 0; i < Ex05.NODES; i++)
            if ((i != me) && a[i]) {
                return true;
            }
        return false;
    }

    private boolean alreadyInQ(int j) {
        for (int i = 0; i < Ex05.NODES; i++)
            if (TokenQ[i] == j)
                inQ = true;
        if (inQ) {
            inQ = false;
            return true;
        } else
            return false;
    }

    private void appendQ(int j) {
        if (!alreadyInQ(j))
            for (int i = 0; i < Ex05.NODES; i++) {
                if (TokenQ[i] == 5) {
                    TokenQ[i] = j;
                    break;
                }
            }
        return;
    }


    private int getNextSite() {
        next = TokenQ[0];
        for (int i = 0; i < Ex05.NODES; i++)
            TempQ[i] = 5;
        for (int i = 0; i < (Ex05.NODES - 1); i++)
            TempQ[i] = TokenQ[(i + 1)];
        TempQ[(Ex05.NODES - 1)] = 5;
        for (int i = 0; i < Ex05.NODES; i++)
            TokenQ[i] = TempQ[i];
        return next;
    }

    private boolean checkSeqNum(int sn, int from) {
        System.out.println("Check sequnce number ho raha hai" + sn + "Seqnce Number of current" + RN[from]);
        if (sn > RN[from] - 1)
            return true;
        else
            return false;
    }


    protected int FindNode(int state) {
        int toNode = 0;
        while ((toNode < Ex05.NODES))
            toNode++;

        return toNode;
    }


    protected void sendMessage(Message message, int to, int parm1, int parm2) {
        send(this.getTopology().findNodeById(to), message);
        System.out.println("Space" + this.getTopology().findNodeById(to));
        receive(message, parm1, parm2);
    }

    // Receive a message sent by another node.
    protected void receive(Message msg, int fromWho, int itsSeqNum) {
        System.out.println("Entering recieve");
        if (msg == tokenMSG) {
            if (state == sendRequestToState) {
                gotToken = true;
                System.out.println("tokenMSG");
                return;
            } else {
                gotToken = true;
                changeState(inCSState);
                System.out.println("recieve chanage state (inCS State)");
                return;
            }
        }
        if (msg == requestMSG) {
            if (checkSeqNum(itsSeqNum, fromWho)) {
                System.out.println("Request Message recieved");
                RN[fromWho] = itsSeqNum;
                if ((RN[fromWho] == (LN[fromWho] + 1)) && (state == enterCSState)) {
                    System.out.println("Request Message recieved"); 
                    gotToken = false;
                    changeState(requestCSState);
                    sendMessage(tokenMSG, fromWho, 0, 0);
                    for (int i = 0; i < Ex05.NODES; i++)
                        sendMessage(TokenQMSG, fromWho, i, TokenQ[i]);
                    for (int i = 0; i < Ex05.NODES; i++)
                        sendMessage(LNMSG, fromWho, i, LN[i]);
                }
//        else
//          if ((RN[fromWho]==(LN[fromWho]+1)) && (state==inCSState)) {
//            wantToEnter = true;
//          }
            }
        }
        if (msg == TokenQMSG)
            TokenQ[fromWho] = itsSeqNum;
        if (msg == LNMSG)
            LN[fromWho] = itsSeqNum;
    }
}

     */




  


