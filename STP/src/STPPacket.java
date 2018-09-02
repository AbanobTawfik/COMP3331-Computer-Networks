public class STPPacket {
    private STPPacketHeader header;
    private byte[] payload;

    public STPPacket(STPPacketHeader header, byte[] payload) {
        this.header = header;
        this.payload = payload;
    }

    public STPPacketHeader getHeader() {
        return header;
    }

    public byte[] getPayload() {
        return payload;
    }
}