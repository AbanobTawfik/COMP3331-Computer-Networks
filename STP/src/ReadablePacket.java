import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class will be used to convert a datagram packet output from a byte streamed object, into a
 * version that uses primitive values that can be easily understood and worked with. This is done through
 * java.nio.ByteBuffer
 */
public class ReadablePacket {
    //below are all the values stored in the header
    private boolean SYN;
    private boolean ACK;
    private boolean FIN;
    private boolean DUP;
    private int checksum;
    private int sequenceNumber;
    private int acknowledgemntNumber;
    private InetAddress sourceIP;
    private InetAddress destIP;
    private int sourcePort;
    private int destPort;
    //and finally the payload of the header
    private byte[] payload;

    /**
     * This method creates a readable packet from a datagram packet directly, by extracting and converting
     * the bytes at the index into more easy to understand values
     *
     * @param srcPacket the datagram packet we are converting into primitive types
     */
    public ReadablePacket(DatagramPacket srcPacket) {
        //if the data at the ACK flag is equal to the defined true, we set ACK to true
        if (srcPacket.getData()[HeaderValues.ACK_POSITION_IN_HEADER] == HeaderValues.TRUE_HEADER) {
            this.ACK = true;
        }
        //otherwise set ACK to false
        else {
            this.ACK = false;
        }
        //if the data at the SYN flag is equal to the defined true, we set SYN to true
        if (srcPacket.getData()[HeaderValues.SYN_POSITION_IN_HEADER] == HeaderValues.TRUE_HEADER) {
            this.SYN = true;
        }
        //otherwise set SYN to false
        else {
            this.SYN = false;
        }
        //if the data at the FIN flag is equal to the defined true, we set FIN to true
        if (srcPacket.getData()[HeaderValues.FIN_POSITION_IN_HEADER] == HeaderValues.TRUE_HEADER) {
            this.FIN = true;
        }
        //otherwise set FIN to false
        else {
            this.FIN = false;
        }
        //if the data at the DYP flag is equal to the defined true, we set DYP to true
        if (srcPacket.getData()[HeaderValues.DUP_POSITION_IN_HEADER] == HeaderValues.TRUE_HEADER) {
            this.DUP = true;
        }
        //otherwise set DUP to false
        else {
            this.DUP = false;
        }
        //now we want to extract from the packet, the 4 byte integers for
        //checksum, sequence number and ack number from the header
        this.checksum = extractFromArray(srcPacket.getData(), HeaderValues.CHECKSUM_POSITION_IN_HEADER);
        this.sequenceNumber = extractFromArray(srcPacket.getData(), HeaderValues.SEQ_POSITION_IN_HEADER);
        this.acknowledgemntNumber = extractFromArray(srcPacket.getData(), HeaderValues.ACKNUM_POSITION_IN_HEADER);
        //now we want to extract the IP source and destination directly from the header
        try {
            this.sourceIP = getInetAddress(srcPacket.getData(), HeaderValues.SRCIP_POSITION_IN_HEADER);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            this.destIP = getInetAddress(srcPacket.getData(), HeaderValues.DSTIP_POSITION_IN_HEADER);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        //extract the 4 byte integer for source and destination port from the header
        this.sourcePort = extractFromArray(srcPacket.getData(), HeaderValues.SRCPRT_POSITION_IN_HEADER);
        this.destPort = extractFromArray(srcPacket.getData(), HeaderValues.DSTPRT_POSITION_IN_HEADER);
        //finally we want to extract the payload from the packet
        this.payload = extractPayLoad(srcPacket.getData());
    }

    /**
     * this method returns the SYN status flag
     *
     * @return the true if SYN is true, else false
     */
    public boolean isSYN() {
        //return directly from header SYN status
        return SYN;
    }

    /**
     * this method will be used to set SYN to either true and false, and modify
     * the readable packet
     *
     * @param SYN true or false boolean whether we have  syn or not
     */
    public void setSYN(boolean SYN) {
        //sets the syn flag as the boolean passed in
        this.SYN = SYN;
    }

    /**
     * this method returns the ACK status flag
     *
     * @return the true if ACK is true, else false
     */
    public boolean isACK() {
        //return directly from header ACK status
        return ACK;
    }

    /**
     * this method will be used to set ACK to either true and false, and modify
     * the readable packet
     *
     * @param ACK true or false boolean whether we have  syn or not
     */
    public void setACK(boolean ACK) {
        //sets the ack flag as the boolean passed in
        this.ACK = ACK;
    }

    /**
     * this method returns the FIN status flag
     *
     * @return the true if FIN is true, else false
     */
    public boolean isFIN() {
        //return directly from header FIN status
        return FIN;
    }

    /**
     * this method will be used to set FIN to either true and false, and modify
     * the readable packet
     *
     * @param FIN true or false boolean whether we have  syn or not
     */
    public void setFIN(boolean FIN) {
        //sets the fin flag as the boolean passed in
        this.FIN = FIN;
    }

    /**
     * this method returns the DUP status flag
     *
     * @return the true if DUP is true, else false
     */
    public boolean isDUP() {
        //return directly from header DUP status
        return DUP;
    }

    /**
     * this method will be used to set DUP to either true and false, and modify
     * the readable packet
     *
     * @param DUP true or false boolean whether we have  syn or not
     */
    public void setDUP(boolean DUP) {
        //sets the dup flag as the boolean passed in
        this.DUP = DUP;
    }

    /**
     * this method returns the checksum value in the header
     *
     * @return checksum value in the header
     */
    public int getChecksum() {
        //return the checksum value inside the header
        return checksum;
    }

    /**
     * this method will be used to modify the checksum value inside the header, might be
     * useful for corruption
     *
     * @param checksum the value of checksum we want to change to
     */
    public void setChecksum(int checksum) {
        //set the checksum value in the packet to be the one passed in
        this.checksum = checksum;
    }

    /**
     * this method returns the sequence number value in the header
     *
     * @return sequence number value in the header
     */
    public int getSequenceNumber() {
        //return the sequenceNumber value inside the header
        return sequenceNumber;
    }

    /**
     * this method will be used to modify the sequenceNumber value inside the header
     *
     * @param sequenceNumber the value of the sequenceNumber we want to change to
     */
    public void setSequenceNumber(int sequenceNumber) {
        //set the sequenceNumber value in the packet to be the one passed in
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * this method returns the acknowledgemnt number value in the header
     *
     * @return acknowledgemnt number value in the header
     */
    public int getAcknowledgemntNumber() {
        //return the acknowledgemntNumber value inside the header
        return acknowledgemntNumber;
    }

    /**
     * this method will be used to modify the acknowledgemntNumber value inside the header
     *
     * @param acknowledgemntNumber the value of the acknowledgemntNumber we want to change to
     */
    public void setAcknowledgemntNumber(int acknowledgemntNumber) {
        //set the acknowledgemntNumber value in the packet to be the one passed in
        this.acknowledgemntNumber = acknowledgemntNumber;
    }

    /**
     * this method returns the sourceIP value in the header
     *
     * @return sourceIP value in the header
     */
    public InetAddress getSourceIP() {
        //return the sourceIP value inside the header
        return sourceIP;
    }

    /**
     * this method will be used to modify the sourceIP value inside the header
     *
     * @param sourceIP the value of the sourceIP we want to change to
     */
    public void setSourceIP(InetAddress sourceIP) {
        //set the sourceIP value in the packet to be the one passed in
        this.sourceIP = sourceIP;
    }

    /**
     * this method returns the destIP value in the header
     *
     * @return destIP value in the header
     */
    public InetAddress getDestIP() {
        //return the destIP value inside the header
        return destIP;
    }

    /**
     * this method will be used to modify the destIP value inside the header
     *
     * @param destIP the value of the destIP we want to change to
     */
    public void setDestIP(InetAddress destIP) {
        //set the destIP value in the packet to be the one passed in
        this.destIP = destIP;
    }

    /**
     * this method returns the sourcePort value in the header
     *
     * @return sourcePort value in the header
     */
    public int getSourcePort() {
        //return the sourcePort value inside the header
        return sourcePort;
    }

    /**
     * this method will be used to modify the sourcePort value inside the header
     *
     * @param sourcePort the value of the sourcePort we want to change to
     */
    public void setSourcePort(int sourcePort) {
        //set the sourcePort value in the packet to be the one passed in
        this.sourcePort = sourcePort;
    }

    /**
     * this method returns the destPort value in the header
     *
     * @return destPort value in the header
     */
    public int getDestPort() {
        //return the destPort value inside the header
        return destPort;
    }

    /**
     * this method will be used to modify the destPort value inside the header
     *
     * @param destPort the value of the destPort we want to change to
     */
    public void setDestPort(int destPort) {
        //set the destPort value in the packet to be the one passed in
        this.destPort = destPort;
    }

    /**
     * this method returns the byte array payload from the packet
     *
     * @return the packet data payload
     */
    public byte[] getPayload() {
        //return the payload of the packet
        return payload;
    }

    /**
     * this method will be used to modify the payload inside the packet
     *
     * @param payload the byte array we are setting out payload to be
     */
    public void setPayload(byte[] payload) {
        //set the payload field to the arguement being passed in
        this.payload = payload;
    }

    /**
     * This method will be used to convert from a byte array into an integer value that
     * can be worked with as a human.
     *
     * @param src the byte array we are converting
     * @return the integer value that the byte array represents
     */
    private int readHeaderValues(byte[] src) {
        //first we want to clear our global byte buffer
        HeaderValues.b.clear();
        try {
            //now we want to try put the byte array into the byte buffer
            HeaderValues.b.put(src);
        }
        //since this will be used by multiple threads specifically in stpsender
        catch (Exception e) {
            //if we receive an error with multiple access we want to clear the buffer
            HeaderValues.b.clear();
            //and try put the value in the buffer again
            HeaderValues.b.put(src);
        }
        //now we want to reset our position of the byte buffer in order to read from the byte buffer
        HeaderValues.b.position(0);
        //create a temporary integer value to hold the output from the byte buffer
        int val;
        try {
            //attempt to read the integer from the byte buffer
            val = HeaderValues.b.getInt();
        }
        //since this will be used by multiple threads specifically in STPSender
        catch (Exception e) {
            //if we receive an error with multiple access we want to reset the buffer position
            HeaderValues.b.position(0);
            //and set the return value = to the value returned from byte buffer
            val = HeaderValues.b.getInt();
        }
        //return our integer converted value
        return val;
    }

    /**
     * this will be the method that calls readHeader values that creates sub-arrays
     * of the different values from the header, and then passes this into readHeaderValues
     *
     * @param src   the source packet data
     * @param start the index start from where we are reading
     * @return the integer value of our sub array
     */
    private int extractFromArray(byte[] src, int start) {
        //pre-defined length in our global class
        int length = HeaderValues.DATA_LENGTH;
        //create a new sub array with length of length
        byte[] ret = new byte[length];
        //start a count for adding
        int count = 0;
        //from the start index --> start index + length
        //we want to copy from the source array that value
        //into our new array
        for (int i = start; i < start + length; i++) {
            ret[count] = src[i];
            count++;
        }
        //return the readHeaderValues of our sub array
        return readHeaderValues(ret);
    }

    /**
     * This method will be used to extract the payload seperated fromt he header
     * from the datagram packet
     *
     * @param src the datagram packet data
     * @return the payload byte array
     */
    private byte[] extractPayLoad(byte[] src) {
        //the payload length is equal to the length of the datagram minus the size of the header in bytes
        int payloadlength = src.length - HeaderValues.PAYLOAD_POSITION_IN_HEADER;
        //we want to create our return payload as a byte array with the length of payload length
        byte[] ret = new byte[payloadlength];
        //start a count since we wont scan from i = 0
        int count = 0;
        //from the end of the header --> final byte in the packet data
        for (int i = HeaderValues.PAYLOAD_POSITION_IN_HEADER;
             i < HeaderValues.PAYLOAD_POSITION_IN_HEADER + payloadlength; i++) {
            //copy the byte value from the original payload index into the return array
            ret[count] = src[i];
            count++;
        }
        //return our payload
        return ret;
    }

    /**
     * This method will return the Inet address from inside the header, as INET address
     * is a different type to integer
     *
     * @param src   the datagram packet we are extracting data from
     * @param start the index we are searching from
     * @return the INET address we are looking for
     * @throws UnknownHostException if we cannot bind to the address
     */
    private InetAddress getInetAddress(byte[] src, int start) throws UnknownHostException {
        //get the length of data from the global variables (ipv$ 4-btye ip address)
        int length = HeaderValues.DATA_LENGTH;
        //set the byte we are extracting IPv4 address from
        byte[] ret = new byte[length];
        //start counter since we do not scan from i  0;
        int count = 0;
        //from the index --> index + data length
        for (int i = start; i < start + length; i++) {
            //we want to copy from the IP index in header to the byte array
            ret[count] = src[i];
            count++;
        }
        //return the IPv4 address from the byte array value in the header
        return InetAddress.getByAddress(ret);
    }

    /**
     * This method will display the packet to standard output
     */
    public void display() {
        //seperator print so we know the packet we are printing
        System.out.println("==================================PACKET INFO==================================");
        //display the flags to standard output
        System.out.println("SYN - " + this.SYN);
        System.out.println("ACK - " + this.ACK);
        System.out.println("DUP - " + this.DUP);
        System.out.println("FIN - " + this.FIN);
        //display the remaining values in the header to stdout
        System.out.println("checksum - " + this.checksum);
        System.out.println("ACKNumber -  " + this.acknowledgemntNumber);
        System.out.println("sequence number -  " + this.sequenceNumber);
        System.out.println("source IP - " + this.sourceIP.getHostAddress());
        System.out.println("destination IP - " + this.destIP.getHostAddress());
        System.out.println("source port - " + this.destPort);
        System.out.println("destination port - " + this.sourcePort);
        //ending seperator so we know when packet eneds
        System.out.println("==================================PACKET INFO==================================");
    }
}