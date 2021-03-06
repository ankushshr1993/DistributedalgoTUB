package util;

public enum NodeStatus {
    DETECTION_IDLE,
    DETECTION_EXPLORED,
    DETECTION_CONSULTED,
    DETECTION_INFORMED,
    ELECTION_RING_IDLE,
    ELECTION_RING_CANDIDATE,
    ELECTION_RING_LOST,
    ELECTION_RING_WON,
    ELECTION_TREE_IDLE,
    ELECTION_TREE_EXPLODED,
    ELECTION_TREE_CONTRACTED,
    ELECTION_TREE_INFORMED,
    ELECTION_TREE_WON,
    TIME_SYNC
}
