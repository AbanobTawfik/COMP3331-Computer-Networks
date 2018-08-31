import java.util.Comparator;

public class PacketComparator implements Comparator<STPPacket> {

    // Overriding compare()method of Comparator
    // for descending order of cgpa
    @Override
    public int compare(STPPacket p1, STPPacket p2) {
        if ( p1.getSequence_Number() < p2.getSequence_Number())
            return 1;
        else if (p1.getSequence_Number() > p2.getSequence_Number())
            return -1;
        return 0;
    }

}