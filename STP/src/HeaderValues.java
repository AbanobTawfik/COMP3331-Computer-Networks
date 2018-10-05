import java.nio.ByteBuffer;

public class HeaderValues {
    static final int DATA_LENGTH = 4;
    static final byte TRUE_HEADER = 0b1;
    static final byte FALSE_HEADER = 0b0;
    static final int ACK_POSITION_IN_HEADER = 0;
    static final int SYN_POSITION_IN_HEADER = 1;
    static final int FIN_POSITION_IN_HEADER = 2;
    static final int DUP_POSITION_IN_HEADER = 3;
    static final int SEQ_POSITION_IN_HEADER = 4;
    static final int CHECKSUM_POSITION_IN_HEADER = 8;
    static final int ACKNUM_POSITION_IN_HEADER = 12;
    static final int DSTIP_POSITION_IN_HEADER = 16;
    static final int SRCIP_POSITION_IN_HEADER = 20;
    static final int SRCPRT_POSITION_IN_HEADER = 24;
    static final int DSTPRT_POSITION_IN_HEADER = 28;
    static final int PAYLOAD_POSITION_IN_HEADER = 32;
    static  final ByteBuffer b = ByteBuffer.allocate(4);

}
