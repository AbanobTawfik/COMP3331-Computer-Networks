import java.net.*;

/**
 * This class will be used to create a packet header in byte form for use in a UDP packet.
 * it will convert all the different types into byte form and store them.
 */
public class STPPacketHeader {
    //this will be all the fields within our header
    //below are the flags for our host state
    private byte SYN;
    private byte ACK;
    private byte FIN;
    private byte DUP;
    //below are now the values for maintenance/error check
    //and sequence/ack number
    private byte[] checksum;
    private byte[] sequenceNumber;
    private byte[] acknowledgemntNumber;
    private byte[] sourceIP;
    private byte[] destIP;
    private byte[] sourcePort;
    private byte[] destPort;

    /**
     * This method will create a packet header based off all the fields passed in
     *
     * @param checksum             the checksum value we are converting to bytes
     * @param sequenceNumber       the sequence number value we are converting to bytes
     * @param acknowledgemntNumber the acknowledgemnt number value we are converting to bytes
     * @param sourceIP             the source ip value we are converting to bytes
     * @param destIP               the dest ip value we are converting to bytes
     * @param sourcePort           the source port value we are converting to bytes
     * @param destPort             the dest port value we are converting to bytes
     * @param SYN                  the syn status we are converting to a byte
     * @param ACK                  the ack status we are converting to a byte
     * @param FIN                  the fin status we are converting to a byte
     * @param DUP                  the dup status we are converting to a byte
     */
    public STPPacketHeader(Integer checksum, Integer sequenceNumber, Integer acknowledgemntNumber,
                           InetAddress sourceIP, InetAddress destIP, Integer sourcePort, Integer destPort,
                           Boolean SYN, Boolean ACK, Boolean FIN, Boolean DUP) {
        //set the checksum to be the byte conversion from integer of our checksum value
        this.checksum = assignIntegerToByte(this.getChecksum(), checksum);
        //set the sequenceNumber to be the byte conversion from integer of our checksum value
        this.sequenceNumber = assignIntegerToByte(this.getSequenceNumber(), sequenceNumber);
        //set the acknowledgemntNumber to be the byte conversion from integer of our checksum value
        this.acknowledgemntNumber = assignIntegerToByte(this.getAcknowledgemntNumber(), acknowledgemntNumber);
        //we can directly convert ip addresses into byte array so we can use that directly
        this.sourceIP = sourceIP.getAddress();
        this.destIP = destIP.getAddress();
        //set the sourcePort to be the byte conversion from integer of our checksum value
        this.sourcePort = assignIntegerToByte(this.sourcePort, sourcePort);
        //set the destination port to be the byte conversion from integer of our checksum value
        this.destPort = assignIntegerToByte(this.destPort, destPort);
        //for all our flags, if the flag is true, we set the byte to be the Byte value of 1
        //otherwise we set the byte value to be 0
        if (SYN == true)
            this.SYN = HeaderValues.TRUE_HEADER;
        else
            this.SYN = HeaderValues.FALSE_HEADER;
        if (ACK == true)
            this.ACK = HeaderValues.TRUE_HEADER;
        else
            this.ACK = HeaderValues.FALSE_HEADER;
        if (FIN == true)
            this.FIN = HeaderValues.TRUE_HEADER;
        else
            this.FIN = HeaderValues.FALSE_HEADER;
        if (DUP == true)
            this.DUP = HeaderValues.TRUE_HEADER;
        else
            this.DUP = HeaderValues.FALSE_HEADER;
    }

    /**
     * This method will return the byte value of the checksum from the header directly
     *
     * @return the byte value of the checksum
     */
    public byte[] getChecksum() {
        //return the byte value of the checksum
        return checksum;
    }

    /**
     * This method will return the byte value of the SequenceNumber from the header directly
     *
     * @return the byte value of the SequenceNumber
     */
    public byte[] getSequenceNumber() {
        //return the byte value of the sequenceNumber
        return sequenceNumber;
    }

    /**
     * This method will return the byte value of the acknowledgemntNumber from the header directly
     *
     * @return the byte value of the acknowledgemntNumber
     */
    public byte[] getAcknowledgemntNumber() {
        //return the byte value of the acknowledgemntNumber
        return acknowledgemntNumber;
    }

    /**
     * This method will return the byte value of the sourceIP from the header directly
     *
     * @return the byte value of the sourceIP
     */
    public byte[] getSourceIP() {
        //return the byte value of the sourceIP
        return sourceIP;
    }

    /**
     * This method will return the byte value of the destIP from the header directly
     *
     * @return the byte value of the destIP
     */
    public byte[] getDestIP() {
        //return the byte value of the destIP
        return destIP;
    }

    /**
     * This method will return the byte value of the sourcePort from the header directly
     *
     * @return the byte value of the sourcePort
     */
    public byte[] getSourcePort() {
        //return the byte value of the sourcePort
        return sourcePort;
    }

    /**
     * This method will return the byte value of the destPort from the header directly
     *
     * @return the byte value of the destPort
     */
    public byte[] getDestPort() {
        //return the byte value of the destPort
        return destPort;
    }

    /**
     * This method will return the byte value of the SYN flag from the header directly
     *
     * @return the byte value of the SYN flag
     */
    public byte isSYN() {
        //return the byte value of the SYN flag
        return SYN;
    }

    /**
     * This method will return the byte value of the ACK flag from the header directly
     *
     * @return the byte value of the ACK flag
     */
    public byte isACK() {
        //return the byte value of the ACK flag
        return ACK;
    }

    /**
     * This method will return the byte value of the FIN flag from the header directly
     *
     * @return the byte value of the FIN flag
     */
    public byte isFIN() {
        //return the byte value of the FIN flag
        return FIN;
    }

    /**
     * This method will return the byte value of the DUP flag from the header directly
     *
     * @return the byte value of the DUP flag
     */
    public byte isDUP() {
        //return the byte value of the DUP flag
        return DUP;
    }


    /**
     * This method will convert an integer into a byte array equivalent, and will store the result into the
     * source array passed in
     *
     * @param source  the byte array we store our integer conversion into
     * @param integer the integer we are converting from
     * @return the byte array we are converting to
     */
    public byte[] assignIntegerToByte(byte[] source, Integer integer) {
        //first clear the byte buffer to allow writing
        HeaderValues.b.clear();
        //create an array to store the result into
        byte[] original;
        try {
            //attempt to put the integer into the byte buffer and retrieve the array
            original = HeaderValues.b.putInt(integer).array();
        } catch (Exception e) {
            //since we multi-thread incase multiple access occurs, we want to
            //reclear the buffer
            HeaderValues.b.clear();
            //and attempt to put the integer into the byte buffer and retrieve the array
            original = HeaderValues.b.putInt(integer).array();
        }
        //since byte buffer does not make deep copies we want to create a new return array to store
        //the return into
        source = new byte[original.length];
        //copy into our array the byte value exactly at each index
        for (int i = 0; i < original.length; i++) {
            source[i] = new Byte(original[i]);
        }
        //return our deep copied byte array
        return source;
    }
}

