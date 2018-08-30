public class Comparator implements Comparator<Packet>{

    // Overriding compare()method of Comparator
    // for descending order of cgpa
    @Override
    public int compare(Packet p1, Packet p2) {
        if (p1.getSequence_Number() < p2.getSequence_Number())
            return 1;
        else if (p1.get > s2.cgpa)
            return -1;
        return 0;
    }

}