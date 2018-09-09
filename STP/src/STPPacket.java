import javafx.util.Pair;

import java.net.*;
import java.util.*;

public class STPPacket {
    private STPPacketHeader header;
    private byte[] payload;
    private DatagramPacket packet = new DatagramPacket(new byte[1024],1024);
    private BytePairs bp;

    public STPPacket(STPPacketHeader header, byte[] payload) {
        this.header = header;
        this.payload = payload;
        ArrayList<Byte> list = new ArrayList<Byte>();
        list.add(header.getSequenceNumber());
        list.add(header.getChecksum());
        list.add(header.getAcknowledgemntNumber());
        addAllBytes(list,header.getDestIP());
        addAllBytes(list,header.getSourceIP());
        list.add(header.getSourcePort());
        list.add(header.getDestPort());
        addAllBytes(list,header.isACK());
        addAllBytes(list,header.isSYN());
        addAllBytes(list,header.isFIN());
        addAllBytes(list,header.isURG());
        addAllBytes(list,payload);
        Byte[] packetData = list.toArray(new Byte[list.size()]);
        packet.setData(primitive(packetData));
        bp = header.getBp();
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

    public BytePairs getBp() {
        return bp;
    }

    public void addAllBytes(ArrayList<Byte> array, byte[] value){
        for(int i = 0; i < value.length; i++)
            array.add(value[i]);
    }

    public  byte[] primitive(Byte[] bytes){
        byte[] ret = new byte[bytes.length];
        for(int i = 0;i<bytes.length;i++)
            ret[i] = bytes[i].byteValue();
        return ret;
    }
}