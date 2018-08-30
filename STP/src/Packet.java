public class Packet{
    private int Sequence_Number;

    public Packet(int sequence_Number) {
        Sequence_Number = sequence_Number;
    }

    public int getSequence_Number() {
        return Sequence_Number;
    }
}