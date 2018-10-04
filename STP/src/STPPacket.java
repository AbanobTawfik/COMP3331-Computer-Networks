
import java.net.*;
import java.util.*;

public class STPPacket {
    private STPPacketHeader header;
    private byte[] payload;
    private DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);

    public STPPacket(ReadablePacket r){
        this.header = new STPPacketHeader(r.getChecksum(),r.getSequenceNumber(),r.getAcknowledgemntNumber(),
                r.getSourceIP(),r.getDestIP(),r.getSourcePort(),r.getDestPort(),r.isSYN(),r.isACK(),r.isFIN(),r.isDUP());
        this.payload = r.getPayload();
        ArrayList<Byte> list = new ArrayList<Byte>();
        list.add(header.isACK());
        list.add(header.isSYN());
        list.add(header.isFIN());
        list.add(header.isDUP());
        addAllBytes(list,header.getSequenceNumber());
        addAllBytes(list,header.getChecksum());
        addAllBytes(list,header.getAcknowledgemntNumber());
        addAllBytes(list, header.getDestIP());
        addAllBytes(list, header.getSourceIP());
        addAllBytes(list,header.getSourcePort());
        addAllBytes(list,header.getDestPort());
        addAllBytes(list, payload);
        Byte[] packetData = list.toArray(new Byte[list.size()]);
        packet.setData(primitive(packetData));
    }

    public STPPacket(STPPacketHeader header, byte[] payload) {
        this.header = header;
        this.payload = payload;
        ArrayList<Byte> list = new ArrayList<Byte>();
        list.add(header.isACK());
        list.add(header.isSYN());
        list.add(header.isFIN());
        list.add(header.isDUP());
        addAllBytes(list,header.getSequenceNumber());
        addAllBytes(list,header.getChecksum());
        addAllBytes(list,header.getAcknowledgemntNumber());
        addAllBytes(list, header.getDestIP());
        addAllBytes(list, header.getSourceIP());
        addAllBytes(list,header.getSourcePort());
        addAllBytes(list,header.getDestPort());
        addAllBytes(list, payload);
        Byte[] packetData = list.toArray(new Byte[list.size()]);
        packet.setData(primitive(packetData));
    }

    public STPPacketHeader getHeader() {
        return header;
    }

    public byte[] getPayload() {
        return payload;
    }

    public DatagramPacket getPacket() {
        return packet;
    }

    public void addAllBytes(ArrayList<Byte> array, byte[] value) {
        for (int i = 0; i < value.length; i++)
            array.add(value[i]);
    }

    public byte[] primitive(Byte[] bytes) {
        byte[] ret = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++)
            ret[i] = bytes[i];
        return ret;
    }
}
//debug code here!!!
//        bp = header.getBp();
