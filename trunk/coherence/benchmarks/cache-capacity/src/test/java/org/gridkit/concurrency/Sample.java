package org.gridkit.concurrency;

public class Sample implements Comparable<Sample> {

    public int threadId;
    public long timestamp;
    public long duration;
    
    public Sample(int threadId, long timestamp, long duration) {
        this.threadId = threadId;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    @Override
    public int compareTo(Sample o) {
        int c = timestamp < o.timestamp ? -1 : timestamp > o.timestamp ? 1 : 0;
        if (c == 0) {
            c = threadId - o.threadId;
        }
        if (c == 0) {
            c = duration < o.duration ? -1 : duration > o.duration ? 1 : 0;
        }
        return c;
    }
}
