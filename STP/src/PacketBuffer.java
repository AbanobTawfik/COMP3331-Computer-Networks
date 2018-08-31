import java.util.*;

public class PacketBuffer {
    private PriorityQueue<STPPacket> buffer = new PriorityQueue<STPPacket>();
    //methods for the packet_buffer

    //method to read the first element value in buffer
    public int peek_sequence_number(){
        return buffer.peek().getSequence_Number();
    }

    public void add(STPPacket p){
        buffer.add(p);
    }

}