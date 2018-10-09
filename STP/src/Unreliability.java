/**
 * This class will be used to store PLD module data
 * to avoid over-crowding sender fields. it will also
 * contain the counter for statistics to print on the log files
 */
public class Unreliability {
    //these are the unreliability factors passed from program arguements in Sender
    private float pDrop;
    private float pDuplicate;
    private float pCorrupt;
    private float pOrder;
    private int maxOrder;
    private float pDelay;
    private int maxDelay;
    private long seed;
    //counters for statistics inside our log files
    private int packetsDropped = 0;
    private int packetsDuplicated = 0;
    private int packetsReordered = 0;
    private int packetsDelayed = 0;
    private int packetsCorrupted = 0;
    private int fastRetransmissions = 0;
    private int PLDTransferredPackets = 0;
    private int packetsTransferred = 0;
    private int packetsTimedOut = 0;
    private int duplicateACKS = 0;

    /**
     * This method will be used to store all PLD error variables into a single class for simplicity sakes
     *
     * @param pDrop      the probability to drop a packet
     * @param pDuplicate the probability to duplicate a packet
     * @param pCorrupt   the probability to corrupt a packet
     * @param pOrder     the probability to re-order a packet
     * @param maxOrder   the maximum number of packets we send before sending our packet we held onto
     * @param pDelay     the probability to delay a packet
     * @param maxDelay   the maximum delay that occurs due to probability of delayed pakcets
     * @param seed       the seed for our random number generator
     */
    public Unreliability(float pDrop, float pDuplicate, float pCorrupt, float pOrder,
                         int maxOrder, float pDelay, int maxDelay, long seed) {
        //set all PLD error values directly from program input
        this.pDrop = pDrop;
        this.pDuplicate = pDuplicate;
        this.pCorrupt = pCorrupt;
        this.pOrder = pOrder;
        this.maxOrder = maxOrder;
        this.pDelay = pDelay;
        this.maxDelay = maxDelay;
        this.seed = seed;
    }

    /**
     * This method will be used to get the probability to drop a packet directly from the PLD module
     *
     * @return the probability to drop a packet
     */
    public float getpDrop() {
        //return the probability to drop a packet
        return pDrop;
    }

    /**
     * This method will be used to get the probability to duplicate a packet directly from the PLD module
     *
     * @return the probability to duplicate a packet
     */
    public float getpDuplicate() {
        //return the probability to duplicate a packet
        return pDuplicate;
    }

    /**
     * This method will be used to get the probability to corrupt a packet directly from the PLD module
     *
     * @return the probability to corrupt a packet
     */
    public float getpCorrupt() {
        //return the probability to corrupt a packet
        return pCorrupt;
    }

    /**
     * This method will be used to get the probability to re-order a packet directly from the PLD module
     *
     * @return the probability to re-order a packet
     */
    public float getpOrder() {
        //return the probability to re-order a packet
        return pOrder;
    }

    /**
     * This method will be used to get the maximum number of packets we send before we send our re-ordered packet
     *
     * @return the maximum packets in re-order
     */
    public int getMaxOrder() {
        //return the maximum packets in re-order
        return maxOrder;
    }

    /**
     * This method will be used to get the probability to delay a packet directly from the PLD module
     *
     * @return the probability to delay a packet
     */
    public float getpDelay() {
        //return the probability to delay a packet
        return pDelay;
    }

    /**
     * This method will be used to get the maximum delay used in pOrder, will be the upperbounds of the delay used
     *
     * @return maximum upperbounds of the delay used in pDelay
     */
    public int getMaxDelay() {
        //return the maximum amount of delay used in pDelay
        return maxDelay;
    }

    /**
     * This method returns the seed used in our random number generator
     *
     * @return the seed used in the random number generator
     */
    public long getSeed() {
        //return the seed used in our random number generator
        return seed;
    }


    /**
     * This method returns the number of packets dropped by the sender in the PLD module
     *
     * @return the number of packets dropped by the sender
     */
    public int getPacketsDropped() {
        //return the amount of packets dropped by the sender
        return packetsDropped;
    }

    /**
     * This method returns the number of packets duplicated by the sender in the PLD module
     *
     * @return the number of packets duplicated by the sender
     */
    public int getPacketsDuplicated() {
        //return the number of packets duplicated by the sender
        return packetsDuplicated;
    }

    /**
     * This method returns the number of packets reordered by the sender in the PLD module
     *
     * @return the number of packets reordered by the sender
     */
    public int getPacketsReordered() {
        //return the number of packets reordered by the sender
        return packetsReordered;
    }

    /**
     * This method returns the number of packets delayed by the sender in the PLD module
     *
     * @return the number of packets delayed by the sender
     */
    public int getPacketsDelayed() {
        //return the number of packets delayed by the sender
        return packetsDelayed;
    }

    /**
     * This method returns the number of packets corrupted by the sender in the PLD module
     *
     * @return the number of packets corrupted by the sender
     */
    public int getPacketsCorrupted() {
        //return the number of packets corrupted by the sender
        return packetsCorrupted;
    }

    /**
     * This method returns the number of fast re-transmissions occured
     *
     * @return the number of fast re-transmissions occured
     */
    public int getFastRetransmissions() {
        //return the number of fast re-transmissions occured
        return fastRetransmissions;
    }

    /**
     * This method will return the number of packets handled by the PLD
     *
     * @return the number of packets handled by the PLD
     */
    public int getPLDTransferredPackets() {
        //return the number of packets handled by the PLD
        return PLDTransferredPackets;
    }

    /**
     * This method will return the number of packets transferred by the sender
     *
     * @return the number of packets transferred by the sender
     */
    public int getPacketsTransferred() {
        //return the number of packets transferred by the sender
        return packetsTransferred;
    }

    /**
     * This method will return the number of packets that timed out
     *
     * @return the number of packets that timed out
     */
    public int getPacketsTimedOut() {
        //return the number of packets that timed out
        return packetsTimedOut;
    }

    /**
     * This method will return the number of duplicate acks received from the receiver
     *
     * @return the number of duplicate acks received from the receiver
     */
    public int getDuplicateACKS() {
        //return the number of duplicate acks received from the receiver
        return duplicateACKS;
    }

    /**
     * This method when called will increment the counter for packets throught he PLD by 1
     */
    public void addPLDTransferred() {
        //increment the pld transfer packet counter by 1
        PLDTransferredPackets++;
    }

    /**
     * This method when called will increment the counter for packets transferred by 1
     */
    public void addPacketsTransferred() {
        //increment the transfer packet counter by 1
        packetsTransferred++;
    }

    /**
     * This method when called will increment the counter for packets dropped by 1
     */
    public void addPacketDropped() {
        //increment the packets dropped counter by 1
        packetsDropped++;
    }

    /**
     * This method when called will increment the counter for packets delayed by 1
     */
    public void addPacketDelayed() {
        //increment the packets delayed counter by 1
        packetsDelayed++;
    }

    /**
     * This method when called will increment the counter for packets corrupted by 1
     */
    public void addPacketCorrupted() {
        //increment the packets delayed counter by 1
        packetsCorrupted++;
    }

    /**
     * This method when called will increment the counter for packets reordered by 1
     */
    public void addPacketReOrdered() {
        //increment the packets reordered counter by 1
        packetsReordered++;
    }

    /**
     * This method when called will increment the counter for packets duplicated by 1
     */
    public void addPacketDuplicated() {
        //increment the packets duplicated counter by 1
        packetsDuplicated++;
    }

    /**
     * This method when called will increment the counter for fast retransmissions by 1
     */
    public void addFastRetransmissions() {
        //increment the fast retransmissions counter by 1
        fastRetransmissions++;
    }

    /**
     * This method when called will increment the counter for number of timeouts by 1
     */
    public void addPacketsTimedOut() {
        //increment the number of timeouts counter by 1
        packetsTimedOut++;
    }

    /**
     * This method when called will increment the counter for number of duplicate acks received by 1
     */
    public void addDuplicateACKS() {
        //increment the number of duplicate acks received counter by 1
        duplicateACKS++;
    }
}
