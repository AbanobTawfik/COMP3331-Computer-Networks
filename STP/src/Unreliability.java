public class Unreliability {
    private long pDrop;
    private long pDuplicate;
    private long pCorrupt;
    private long pOrder;
    private int maxOrder;
    private long pDelay;
    private long maxDelay;
    private long seed;

    public Unreliability(long pDrop, long pDuplicate, long pCorrupt, long pOrder, int maxOrder, long pDelay, long maxDelay, long seed) {
        this.pDrop = pDrop;
        this.pDuplicate = pDuplicate;
        this.pCorrupt = pCorrupt;
        this.pOrder = pOrder;
        this.maxOrder = maxOrder;
        this.pDelay = pDelay;
        this.maxDelay = maxDelay;
        this.seed = seed;
    }

    public long getpDrop() {
        return pDrop;
    }

    public long getpDuplicate() {
        return pDuplicate;
    }

    public long getpCorrupt() {
        return pCorrupt;
    }

    public long getpOrder() {
        return pOrder;
    }

    public int getMaxOrder() {
        return maxOrder;
    }

    public long getpDelay() {
        return pDelay;
    }

    public long getMaxDelay() {
        return maxDelay;
    }

    public long getSeed() {
        return seed;
    }
}
