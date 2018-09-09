import java.util.Comparator;

public class PacketComparator implements Comparator<STPPacket> {

    // Overriding compare()method of Comparator
    // for descending order of cgpa
    @Override
    public int compare(STPPacket p1, STPPacket p2) {
        if ( HeaderValues.b.put(p1.getHeader().getSequenceNumber()).getInt() < HeaderValues.b.put(p2.getHeader().getSequenceNumber()).getInt())
            return 1;
        else if (HeaderValues.b.put(p1.getHeader().getSequenceNumber()).getInt() > HeaderValues.b.put(p2.getHeader().getSequenceNumber()).getInt())
            return -1;
        return 0;
    }

}