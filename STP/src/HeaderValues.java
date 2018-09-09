import java.nio.ByteBuffer;

public class HeaderValues {
    public static final byte TRUE_HEADER = 0b1;
    public static final byte FALSE_HEADER = 0b0;
    public static final int ACK_POSITION_IN_HEADER = 0;
    public static final int SYN_POSITION_IN_HEADER = 1;
    public static final int FIN_POSITION_IN_HEADER = 2;
    public static final int URG_POSITION_IN_HEADER = 3;
    public static final ByteBuffer b = ByteBuffer.allocate(4);

}
