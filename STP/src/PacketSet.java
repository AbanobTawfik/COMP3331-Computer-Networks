import java.util.ArrayList;
import java.util.Comparator;

/**
 * This class will be used as a set to store packets received from the sender. the class will maintain
 * order within sequence number of the packet. the class will also not add any duplicate packets to the set
 * an array list is used to maintain the state of our set
 */
public class PacketSet {
    //the arraylist that will store the packets in the set
    private ArrayList<ReadablePacket> set = new ArrayList<ReadablePacket>();

    /**
     * this method will add a packet to the set based on packet details.
     * first we check if the packet exists in the set, if it does we return
     * if the packet is null, we return. otherwise we will add it to the arraylist
     * and then sort the array list
     *
     * @param r the packet we are attempting to add to the set
     */
    public void add(ReadablePacket r) {
        //if the packet is null, return
        if (r == null)
            return;
        //scan through the current set
        for (ReadablePacket r1 : set) {
            //if the packet exists in the set, return
            if (r.getSequenceNumber() == r1.getSequenceNumber())
                return;
        }
        //otherwise add the packet to the set
        set.add(r);
        //sort the set
        sortSet();
    }

    /**
     * this method will remove a packet from the set provided the packet exists within the set.
     *
     * @param r the packet we are attempting to remove from the set
     */
    public void remove(ReadablePacket r) {
        //if the packet doesnt exist within our set, return
        if (!set.contains(r))
            return;
        //otherwise we remove the packet from the set, and sort the set to maintain order
        set.remove(r);
        sortSet();

    }

    /**
     * Get readable packet.
     *
     * @param index the index
     * @return the readable packet
     */
    public ReadablePacket get(int index) {
        if (index < 0 || index >= set.size())
            return null;
        return set.get(index);
    }

    /**
     * Last int.
     *
     * @return the int
     */
    public int last() {
        if (set.size() == 0)
            return 0;
        if (set.size() == 1)
            return set.get(0).getSequenceNumber();
        return set.get(set.size() - 1).getSequenceNumber();
    }

    /**
     * Get readable packet.
     *
     * @param r the r
     * @return the readable packet
     */
    public ReadablePacket get(ReadablePacket r) {
        if (!set.contains(r))
            return null;
        if (set.indexOf(r) == -1)
            return null;
        return set.get(set.indexOf(r));
    }

    /**
     * Size int.
     *
     * @return the int
     */
    public int size() {
        return set.size();
    }

    /**
     * Index of int.
     *
     * @param r the r
     * @return the int
     */
    public int indexOf(ReadablePacket r) {
        return set.indexOf(r);
    }


    /**
     * Get array list array list.
     *
     * @return the array list
     */
    public ArrayList<ReadablePacket> getArrayList() {
        return set;
    }

    /**
     * Contains boolean.
     *
     * @param r the r
     * @return the boolean
     */
    public boolean contains(ReadablePacket r) {
        for (ReadablePacket r1 : set) {
            if (r1.getSequenceNumber() == r.getSequenceNumber())
                return true;
        }
        return false;
    }

    /**
     * Sort set.
     */
    public void sortSet() {
        set.sort(new Comparator<ReadablePacket>() {

            @Override
            public int compare(ReadablePacket o1, ReadablePacket o2) {
                if (o1.getSequenceNumber() < o2.getSequenceNumber())
                    return -1;
                if (o1.getSequenceNumber() > o2.getSequenceNumber())
                    return 1;
                return 0;
            }
        });
    }


}