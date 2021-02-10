package exercise01;

public class LocalClock {

    private double currentTime;
    private final double timerResolution;
    private final double deviation;
    private double clockSpeed;

    public LocalClock(double timerResolution, double deviation) {
        this.timerResolution = timerResolution;
        this.deviation = deviation;
        clockSpeed = timerResolution + deviation;
    }

    public void onClock() {
        currentTime += clockSpeed;
    }

    public double getCurrentTime() {
        return currentTime;
    }

    public String getCurrentTimeString() {
        return String.valueOf(currentTime);
    }

    public void adjustTime(double shift) {
        if (shift > 0) {
            currentTime += shift;
        }
        clockSpeed *= (currentTime + shift) / currentTime;
    }

    public double getClockSpeed() {
        return clockSpeed;
    }

    public void setClockSpeed(double clockSpeed) {
        this.clockSpeed = clockSpeed;
    }
}
