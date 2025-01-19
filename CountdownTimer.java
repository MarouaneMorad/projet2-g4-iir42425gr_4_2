public class CountdownTimer {
    private long startTime;
    private long totalDuration;

    public CountdownTimer(long totalDuration) {
        this.startTime = System.currentTimeMillis();
        this.totalDuration = totalDuration;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getTotalDuration() {
        return totalDuration;
    }
}
