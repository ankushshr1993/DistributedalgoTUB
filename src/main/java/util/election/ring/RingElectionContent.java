package util.election.ring;

public class RingElectionContent {
    private Integer value;
    private Integer num;
    private Integer maxnum;

    public RingElectionContent(int value, int num, int maxnum) {
        this.value = value;
        this.num = num;
        this.maxnum = maxnum;
    }

    public RingElectionContent(RingElectionContent content) {
        this.value = content.getValue();
        this.num = content.getNum();
        this.maxnum = content.getMaxnum();
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getMaxnum() {
        return maxnum;
    }

    public void setMaxnum(int maxnum) {
        this.maxnum = maxnum;
    }
}
