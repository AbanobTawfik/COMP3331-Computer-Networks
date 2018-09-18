public class Unreliability {
    private float pDrop;
    private float pDuplicate;
    private float pCorrupt;
    private float pOrder;
    private int maxOrder;
    private float pDelay;
    private float maxDelay;
    private float seed;

    public Unreliability(float pDrop, float pDuplicate, float pCorrupt, float pOrder, int maxOrder, float pDelay, float maxDelay, float seed) {
        this.pDrop = pDrop;
        this.pDuplicate = pDuplicate;
        this.pCorrupt = pCorrupt;
        this.pOrder = pOrder;
        this.maxOrder = maxOrder;
        this.pDelay = pDelay;
        this.maxDelay = maxDelay;
        this.seed = seed;
    }

    public float getpDrop() {
        return pDrop;
    }

    public float getpDuplicate() {
        return pDuplicate;
    }

    public float getpCorrupt() {
        return pCorrupt;
    }

    public float getpOrder() {
        return pOrder;
    }

    public int getMaxOrder() {
        return maxOrder;
    }

    public float getpDelay() {
        return pDelay;
    }

    public float getMaxDelay() {
        return maxDelay;
    }

    public float getSeed() {
        return seed;
    }
}
