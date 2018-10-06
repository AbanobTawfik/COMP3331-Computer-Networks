import java.util.*;

public class PacketBuffer {
    private Comparator<ReadablePacket> c = new PacketComparator();
    private PriorityQueue<ReadablePacket> buffer;
    private int maxCapacity;
    //methods for the packet_buffer

    public PacketBuffer(int maxCapacity) {
        this.buffer = new PriorityQueue<ReadablePacket>(maxCapacity, this.c);
        this.maxCapacity = maxCapacity;
    }

    //method to read the first element value in buffer
    public int peek_sequence_number(){
        if(!buffer.isEmpty())
            return buffer.peek().getSequenceNumber();
        return -1;
    }

    public void add(ReadablePacket p){
        buffer.offer(p);
    }

    public ReadablePacket remove(){
        return buffer.poll();
    }

    public int spaceRemaining(){
        return this.maxCapacity - this.buffer.size();
    }

    public void addConditionally(PacketSet src){
        if(src.size() == 0 || buffer.size() == 0)
            return;
        while(src.get(src.size()-1).getSequenceNumber() > peek_sequence_number()){
            src.add(remove());
        }
    }

    public PriorityQueue<ReadablePacket> getBuffer(){
        return this.buffer;
    }

    public void display(){
        int count = 0;
        for(ReadablePacket r : buffer){
            System.out.println("packet #" + count + "seq number - " + r.getSequenceNumber());
            count++;
        }
    }
}