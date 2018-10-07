public class Unreliability {
    private float pDrop;
    private float pDuplicate;
    private float pCorrupt;
    private float pOrder;
    private int maxOrder;
    private float pDelay;
    private int maxDelay;
    private long seed;
    private int packetsDropped = 0;
    private int packetsDuplicated = 0;
    private int packetsReordered = 0;
    private int packetsDelayed = 0;
    private int packetsCorrupted = 0;
    private int fastRetransmissions = 0;

    public Unreliability(float pDrop, float pDuplicate, float pCorrupt, float pOrder, int maxOrder, float pDelay, int maxDelay, long seed) {
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

    public int getMaxDelay() {
        return maxDelay;
    }

    public long getSeed() {
        return seed;
    }


    public int getPacketsDropped() {
        return packetsDropped;
    }

    public int getPacketsDuplicated() {
        return packetsDuplicated;
    }

    public int getPacketsReordered() {
        return packetsReordered;
    }

    public int getPacketsDelayed() {
        return packetsDelayed;
    }

    public int getPacketsCorrupted() {
        return packetsCorrupted;
    }

    public int getFastRetransmissions() {
        return fastRetransmissions;
    }

    public void addPacketDropped() {
        packetsDropped++;
    }

    public void addPacketDelayed() {
        packetsDelayed++;
    }

    public void addPacketCorrupted() {
        packetsCorrupted++;
    }

    public void addPacketReOrdered() {
        packetsReordered++;
    }

    public void addPacketDuplicated() {
        packetsDuplicated++;
    }

    public void addFastRetransmissions() {
        fastRetransmissions++;
    }
}
