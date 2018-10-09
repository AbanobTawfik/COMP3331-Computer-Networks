import java.nio.ByteBuffer;

/**
 * This class will be used as a global variable class to maintain
 * packet size, index's within the header, and store a global byte buffer
 * for use throughout all the program
 */
public class HeaderValues {
    //length of data will be 4 (integer, IP)
    static final int DATA_LENGTH = 4;
    //true = byte value 1
    static final byte TRUE_HEADER = 0b1;
    //false = byte value 0
    static final byte FALSE_HEADER = 0b0;
    //now we set our index's of our header based on size + position
    static final int ACK_POSITION_IN_HEADER = 0;
    static final int SYN_POSITION_IN_HEADER = 1;
    static final int FIN_POSITION_IN_HEADER = 2;
    static final int DUP_POSITION_IN_HEADER = 3;
    static final int SEQ_POSITION_IN_HEADER = 4;
    //since sequence number has size 4 (integer) checksum will be position 8 and so on
    //and so on
    static final int CHECKSUM_POSITION_IN_HEADER = 8;
    static final int ACKNUM_POSITION_IN_HEADER = 12;
    static final int DSTIP_POSITION_IN_HEADER = 16;
    static final int SRCIP_POSITION_IN_HEADER = 20;
    static final int SRCPRT_POSITION_IN_HEADER = 24;
    static final int DSTPRT_POSITION_IN_HEADER = 28;
    static final int PAYLOAD_POSITION_IN_HEADER = 32;
    //a global byte buffer to convert between byte array and integer/primitive values
    static final ByteBuffer b = ByteBuffer.allocate(4);
}
