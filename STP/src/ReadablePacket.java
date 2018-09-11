import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ReadablePacket {
    private boolean SYN;
    private boolean ACK;
    private boolean FIN;
    private boolean URG;
    private int checksum;
    private int sequenceNumber;
    private int acknowledgemntNumber;
    private InetAddress sourceIP;
    private InetAddress destIP;
    private int sourcePort;
    private int destPort;
    private byte[] payload;

    public ReadablePacket(DatagramPacket srcPacket) {
        if (srcPacket.getData()[HeaderValues.ACK_POSITION_IN_HEADER] == HeaderValues.TRUE_HEADER)
            this.ACK = true;
        else
            this.ACK = false;
        if (srcPacket.getData()[HeaderValues.SYN_POSITION_IN_HEADER] == HeaderValues.TRUE_HEADER)
            this.SYN = true;
        else
            this.SYN = false;
        if (srcPacket.getData()[HeaderValues.FIN_POSITION_IN_HEADER] == HeaderValues.TRUE_HEADER)
            this.FIN = true;
        else
            this.FIN = false;
        if (srcPacket.getData()[HeaderValues.URG_POSITION_IN_HEADER] == HeaderValues.TRUE_HEADER)
            this.URG = true;
        else
            this.URG = false;
        this.checksum = extractFromArray(srcPacket.getData(), HeaderValues.CHECKSUM_POSITION_IN_HEADER);
        this.sequenceNumber = extractFromArray(srcPacket.getData(), HeaderValues.SEQ_POSITION_IN_HEADER);
        this.acknowledgemntNumber = extractFromArray(srcPacket.getData(), HeaderValues.ACKNUM_POSITION_IN_HEADER);
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
        this.sourcePort = extractFromArray(srcPacket.getData(), HeaderValues.SRCPRT_POSITION_IN_HEADER);
        this.destPort = extractFromArray(srcPacket.getData(), HeaderValues.DSTPRT_POSITION_IN_HEADER);
        this.payload = extractPayLoad(srcPacket.getData());
    }

    public boolean isSYN() {
        return SYN;
    }

    public void setSYN(boolean SYN) {
        this.SYN = SYN;
    }

    public boolean isACK() {
        return ACK;
    }

    public void setACK(boolean ACK) {
        this.ACK = ACK;
    }

    public boolean isFIN() {
        return FIN;
    }

    public void setFIN(boolean FIN) {
        this.FIN = FIN;
    }

    public boolean isURG() {
        return URG;
    }

    public void setURG(boolean URG) {
        this.URG = URG;
    }

    public int getChecksum() {
        return checksum;
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public int getAcknowledgemntNumber() {
        return acknowledgemntNumber;
    }

    public void setAcknowledgemntNumber(int acknowledgemntNumber) {
        this.acknowledgemntNumber = acknowledgemntNumber;
    }

    public InetAddress getSourceIP() {
        return sourceIP;
    }

    public void setSourceIP(InetAddress sourceIP) {
        this.sourceIP = sourceIP;
    }

    public InetAddress getDestIP() {
        return destIP;
    }

    public void setDestIP(InetAddress destIP) {
        this.destIP = destIP;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    public int getDestPort() {
        return destPort;
    }

    public void setDestPort(int destPort) {
        this.destPort = destPort;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public int readHeaderValues(byte[] src) {
        HeaderValues.b.clear();
        HeaderValues.b.put(src);
        HeaderValues.b.position(0);
        int val = new Integer(HeaderValues.b.getInt());
        return val;
    }

    public int extractFromArray(byte[] src, int start) {
        int length = HeaderValues.DATA_LENGTH;
        byte[] ret = new byte[length];
        int count = 0;
        for (int i = start; i < start + length; i++) {
            ret[count] = src[i];
            count++;
        }
        return readHeaderValues(ret);
    }

    public byte[] extractPayLoad(byte[] src) {
        int payloadlength = src.length - HeaderValues.PAYLOAD_POSITION_IN_HEADER;
        byte[] ret = new byte[payloadlength];
        int count = 0;
        for (int i = HeaderValues.PAYLOAD_POSITION_IN_HEADER; i < payloadlength; i++) {
            ret[count] = src[i];
            count++;
        }
        return ret;
    }

    public InetAddress getInetAddress(byte[] src, int start) throws UnknownHostException {
        int length = HeaderValues.DATA_LENGTH;
        byte[] ret = new byte[length];
        int count = 0;
        for (int i = start; i < start + length; i++) {
            ret[count] = src[i];
            count++;
        }
        return InetAddress.getByAddress(ret);
    }

    public void display() {
        System.out.println("==================================PACKET INFO==================================");
        System.out.println("SYN - " + this.SYN);
        System.out.println("ACK - " + this.ACK);
        System.out.println("URG - " + this.URG);
        System.out.println("FIN - " + this.FIN);
        System.out.println("checksum - " + this.checksum);
        System.out.println("ACKNumber -  " + this.acknowledgemntNumber);
        System.out.println("sequence number -  " + this.sequenceNumber);
        System.out.println("source IP - " + this.sourceIP.getHostAddress());
        System.out.println("destination IP - " + this.destIP.getHostAddress());
        System.out.println("source port - " + this.destPort);
        System.out.println("destination port - " + this.sourcePort);
        System.out.println("==================================PACKET INFO==================================");
    }
}
