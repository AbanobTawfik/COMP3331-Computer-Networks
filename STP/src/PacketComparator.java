import java.util.Comparator;

public class PacketComparator implements Comparator<STPPacket> {

    // Overriding compare()method of Comparator
    // for descending order of cgpa
    @Override
    public int compare(STPPacket p1, STPPacket p2) {
        if ( p1.getHeader().getSequenceNumber() < p2.getHeader().getSequenceNumber())
            return 1;
        else if (p1.getHeader().getSequenceNumber() > p2.getHeader().getSequenceNumber())
            return -1;
        return 0;
    }

}