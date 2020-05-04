package com.zuel.syzc.spark.crowd;

public class InOutTimeNumber{
    private long timestamp;
    private long inflow;
    private long outflow;

    public InOutTimeNumber() {
    }

    public InOutTimeNumber(long timestamp, long inflow, long outflow) {
        this.timestamp = timestamp;
        this.inflow = inflow;
        this.outflow = outflow;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getInflow() {
        return inflow;
    }

    public void setInflow(long inflow) {
        this.inflow = inflow;
    }

    public long getOutflow() {
        return outflow;
    }

    public void setOutflow(long outflow) {
        this.outflow = outflow;
    }

    @Override
    public String toString() {
        return "InOutTimeNumber{" +
                "timestamp=" + timestamp +
                ", inflow=" + inflow +
                ", outflow=" + outflow +
                '}';
    }
}
