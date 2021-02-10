package util;

public class MessageCounter {

    private static int generalCount = 0;
    private static int detectionCount = 0;
    private static int electionCount = 0;
    private static int unknownCount = 0;

    public static void increaseGeneralCount() {
        generalCount++;
    }
    public static void increaseDetectionCount() {
        detectionCount++;
    }
    public static void increaseElectionCount() {
        electionCount++;
    }
    public static void increaseUnknownCount() {
        unknownCount++;
    }

    public static void increaseGeneralCount(int i) {
        generalCount += i;
    }

    public static void printGeneralCount(){
        System.out.println("Number of messages sent: " + generalCount);
    }


    public static void printDetectionAndElectionMessages(){
        System.out.println("Number of messages sent:");
        System.out.println("in the Detection phase: " + detectionCount);
        System.out.println("in the Election phase: " + electionCount);
        System.out.println("Total messages sent: " + (detectionCount + electionCount));
        System.out.println("Additional unknown messages sent: " + unknownCount);
    }

    public static void resetCounters() {
        generalCount = 0;
        detectionCount = 0;
        electionCount = 0;
        unknownCount = 0;
    }
}
