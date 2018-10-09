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
     * this method will return the packet from the index we are requesting
     *
     * @param index the index of the packet we are retrieving
     * @return the packet we requested
     */
    public ReadablePacket get(int index) {
        //if the index is less than 0 or the index is greater than current limit
        //return null
        if (index < 0 || index >= set.size())
            return null;
        //otherwise we want to return the packet from that index
        return set.get(index);
    }

    /**
     * This method will return the last sequence number inside the set
     * it will go to the last index and return that sequence number
     *
     * @return the last sequence number of the packet in the set
     */
    public int last() {
        //if the set has no packets, return 0
        if (set.size() == 0)
            return 0;
        //if the set has only 1 packet, we want to return the sequence number of the first packet
        if (set.size() == 1)
            return set.get(0).getSequenceNumber();
        //otherwise return the last index's sequence number
        return set.get(set.size() - 1).getSequenceNumber();
    }

    /**
     * this method will look for and return a packet from the list
     *
     * @param r the packet we are searching for
     * @return the packet we are searching for inside the SET
     */
    public ReadablePacket get(ReadablePacket r) {
        //if the packet doesn't exist within the set, return null
        if (!set.contains(r))
            return null;
        //if the packet has index -1 means its not in the set, return null
        if (set.indexOf(r) == -1)
            return null;
        //otherwise return the packet at the index of the object in the set
        return set.get(set.indexOf(r));
    }

    /**
     * This method will return the size of the current set
     *
     * @return the size of the current set
     */
    public int size() {
        //return the size of the current set
        return set.size();
    }

    /**
     * This method will return the index of a packet inside the set
     *
     * @param r the packet we want the index of
     * @return the index of the packet
     */
    public int indexOf(ReadablePacket r) {
        //return the index of the packet we are looking for
        return set.indexOf(r);
    }


    /**
     * This method will return the arraylist which is also a set due to conditions and methods placed
     *
     * @return the array list version of our set that allows traversal
     */
    public ArrayList<ReadablePacket> getArrayList() {
        //return our set
        return set;
    }

    /**
     * This method will return a boolean which checks if the packet exists within the set
     *
     * @param r the packet we are checking existence of
     * @return true if the packet exists in the set, false otherwise
     */
    public boolean contains(ReadablePacket r) {
        //scan through the current set and we are comparing based on sequence number
        for (ReadablePacket r1 : set) {
            //if the sequence number of the packet matches the one in the array
            if (r1.getSequenceNumber() == r.getSequenceNumber()) {
                //return true
                return true;
            }
        }
        //otherwise return false
        return false;
    }

    /**
     * This method will sort the set when called based on our comparator
     * it will sort the set in ascending order of sequence number to maintain
     * correct order of packets
     */
    public void sortSet() {
        //we are sorting based on our new custom comparator that compares sequence numbers, and
        //will sort in ASCENDING order
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