import java.net.*;
import java.util.*;

/**
 * This class will create a packet with both a header, and a payload attatched. this class also allows
 * easy conversion into a datagram that contains the packet itself as the data, ready for sending
 */
public class STPPacket {
    //the header for the packet
    private STPPacketHeader header;
    //the payload for the packet
    private byte[] payload;
    //the datagram version of our packet
    private DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);

    /**
     * This method will create a STP packet from a readable packet which allows easy conversion between the two
     *
     * @param r the readable packet we are creating our binary based packet from
     */
    public STPPacket(ReadablePacket r) {
        //first we want to create our header from the readable packets header
        this.header = new STPPacketHeader(r.getChecksum(), r.getSequenceNumber(), r.getAcknowledgemntNumber(),
                r.getSourceIP(), r.getDestIP(), r.getSourcePort(), r.getDestPort(), r.isSYN(), r.isACK(), r.isFIN(),
                r.isDUP());
        //we want to set the payload to be the payload in the readable packet
        this.payload = r.getPayload();
        //now we want to create a new byte list to store data for our packet
        ArrayList<Byte> list = new ArrayList<Byte>();
        //we want to add the byte values of the flag to the list
        list.add(header.isACK());
        list.add(header.isSYN());
        list.add(header.isFIN());
        list.add(header.isDUP());
        //add all the bytes in the byte array for the 4 byte integers, IP addresses
        addAllBytes(list, header.getSequenceNumber());
        addAllBytes(list, header.getChecksum());
        addAllBytes(list, header.getAcknowledgemntNumber());
        addAllBytes(list, header.getDestIP());
        addAllBytes(list, header.getSourceIP());
        addAllBytes(list, header.getSourcePort());
        addAllBytes(list, header.getDestPort());
        //finally ad all the bytes in the payload to the list
        addAllBytes(list, payload);
        //now we want to get a byte array return from the list
        Byte[] packetData = list.toArray(new Byte[list.size()]);
        //set the data for the packet to be the byte array
        packet.setData(primitive(packetData));
    }

    /**
     * This method will create a STP packet from a header and payload unlike method above that uses a readable packet
     * to extract these fields
     *
     * @param header  the header for our packet
     * @param payload the payload for our packet
     */
    public STPPacket(STPPacketHeader header, byte[] payload) {
        //set the header and payload to the arguements passed in
        this.header = header;
        this.payload = payload;
        //now we want to create a new byte list to store data for our packet
        ArrayList<Byte> list = new ArrayList<Byte>();
        //we want to add the byte values of the flag to the list
        list.add(header.isACK());
        list.add(header.isSYN());
        list.add(header.isFIN());
        list.add(header.isDUP());
        //add all the bytes in the byte array for the 4 byte integers, IP addresses
        addAllBytes(list, header.getSequenceNumber());
        addAllBytes(list, header.getChecksum());
        addAllBytes(list, header.getAcknowledgemntNumber());
        addAllBytes(list, header.getDestIP());
        addAllBytes(list, header.getSourceIP());
        addAllBytes(list, header.getSourcePort());
        addAllBytes(list, header.getDestPort());
        //finally ad all the bytes in the payload to the list
        addAllBytes(list, payload);
        //now we want to get a byte array return from the list
        Byte[] packetData = list.toArray(new Byte[list.size()]);
        //set the data for the packet to be the byte array
        packet.setData(primitive(packetData));
    }


    /**
     * This method will return the header of our current packet
     *
     * @return the header of the current packet
     */
    public STPPacketHeader getHeader() {
        //return the header of our curent packet
        return header;
    }

    /**
     * This method will return the data payload within our packet
     *
     * @return the data payload within our packet
     */
    public byte[] getPayload() {
        //return the data payload within the packet
        return payload;
    }

    /**
     * This method will return a datagram version of the packet allowing for instant send/receive
     * since the packet is already made on creation
     *
     * @return the UDP packet with our header + payload as data
     */
    public DatagramPacket getPacket() {
        //return the UDP packet
        return packet;
    }

    /**
     * This method will add all bytes in a byte array to a list of bytes
     *
     * @param array the list we are adding bytes to
     * @param value the byte array we are extracting from
     */
    public void addAllBytes(ArrayList<Byte> array, byte[] value) {
        //foreach byte in the array of bytes
        //add a copy into the list
        for (int i = 0; i < value.length; i++)
            array.add(value[i]);
    }

    /**
     * This method will convert from Byte (primitive) into byte which is used by a UDP datagram
     *
     * @param bytes the primitive version of our Byte array
     * @return the byte array that can be used by the UDP datagram
     */
    public byte[] primitive(Byte[] bytes) {
        //we want to create a new byte array with the same length
        byte[] ret = new byte[bytes.length];
        //for each element we want to do a direct copy from the primitive version
        for (int i = 0; i < bytes.length; i++)
            ret[i] = bytes[i];
        //return the updated byte array
        return ret;
    }
}