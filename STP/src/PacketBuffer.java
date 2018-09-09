import java.util.*;

public class PacketBuffer {
    private Comparator<STPPacket> c = new PacketComparator();
    private PriorityQueue<STPPacket> buffer;
    private int maxCapacity;
    //methods for the packet_buffer

    public PacketBuffer(int maxCapacity) {
        this.buffer = new PriorityQueue<STPPacket>(maxCapacity, this.c);
        this.maxCapacity = maxCapacity;
    }

    //method to read the first element value in buffer
    public int peek_sequence_number(){
        if(!buffer.isEmpty())
            return HeaderValues.b.put(buffer.peek().getHeader().getSequenceNumber()).getInt();
        return -1;
    }

    public void add(STPPacket p){
        buffer.offer(p);
    }

    public STPPacket remove(){
        return buffer.poll();
    }

    public int spaceRemaining(){
        return this.maxCapacity - this.buffer.size();
    }

}