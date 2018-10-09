import java.util.Comparator;

public class PacketComparator implements Comparator<ReadablePacket> {


    @Override
    public int compare(ReadablePacket p1, ReadablePacket p2) {
        if (p1.getSequenceNumber() < p2.getSequenceNumber())
            return -1;
        else if (p1.getSequenceNumber() > p2.getSequenceNumber())
            return 1;
        return 0;
    }

}