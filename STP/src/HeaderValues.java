import java.nio.ByteBuffer;

public class HeaderValues {
    public static final int DATA_LENGTH = 4;
    public static final byte TRUE_HEADER = 0b1;
    public static final byte FALSE_HEADER = 0b0;
    public static final int ACK_POSITION_IN_HEADER = 0;
    public static final int SYN_POSITION_IN_HEADER = 1;
    public static final int FIN_POSITION_IN_HEADER = 2;
    public static final int URG_POSITION_IN_HEADER = 3;
    public static final int SEQ_POSITION_IN_HEADER = 4;
    public static final int CHECKSUM_POSITION_IN_HEADER = 8;
    public static final int ACKNUM_POSITION_IN_HEADER = 12;
    public static final int DSTIP_POSITION_IN_HEADER = 16;
    public static final int SRCIP_POSITION_IN_HEADER = 20;
    public static final int SRCPRT_POSITION_IN_HEADER = 24;
    public static final int DSTPRT_POSITION_IN_HEADER = 28;
    public static final int PAYLOAD_POSITION_IN_HEADER = 32;

    public static final ByteBuffer b = ByteBuffer.allocate(4);

}
