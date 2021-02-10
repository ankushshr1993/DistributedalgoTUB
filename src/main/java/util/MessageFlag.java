package util;

public enum MessageFlag {

    DETECTION_EXPLORER,
    DETECTION_ECHO,
    DETECTION_INFORMATION,
    ELECTION_RING_FROM,
    ELECTION_RING_OK,
    ELECTION_RING_NO,
    ELECTION_TREE_EXPLOSION,
    ELECTION_TREE_CONTRACTION,
    ELECTION_TREE_INFORMATION,
    BERKELEY_GET_TIME,
    BERKELEY_TIME_VALUE,
    BERKELEY_SET_TIME
    ;

    public boolean equals(String flag) {
        return this.toString().equals(flag);
    }
}
