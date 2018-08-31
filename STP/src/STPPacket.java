public class STPPacket {
    private int Sequence_Number;

    public STPPacket(int sequence_Number) {
        Sequence_Number = sequence_Number;
    }

    public int getSequence_Number() {
        return Sequence_Number;
    }
}