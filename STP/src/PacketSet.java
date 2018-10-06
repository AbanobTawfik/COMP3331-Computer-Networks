import java.util.ArrayList;
import java.util.Comparator;

public class PacketSet {
    private ArrayList<ReadablePacket> set = new ArrayList<ReadablePacket>();

    public void add(ReadablePacket r){
        if(r == null)
            return;
        for(ReadablePacket r1: set){
            if(r.getSequenceNumber() == r1.getSequenceNumber())
                return;
        }
            set.add(r);
        sortSet();
    }

    public void remove(ReadablePacket r){
        if(!set.contains(r))
            return;
        set.remove(r);
        sortSet();

    }

    public ReadablePacket get(int index){
        if(index < 0 || index >= set.size())
            return null;
        return set.get(index);
    }

    public int last(){
        if(set.size() == 0)
            return 0;
        if(set.size() == 1)
            return set.get(0).getSequenceNumber();
        return set.get(set.size()-1).getSequenceNumber();
    }

    public ReadablePacket get(ReadablePacket r){
        if(!set.contains(r))
            return null;
        if(set.indexOf(r) == -1)
            return null;
        return set.get(set.indexOf(r));
    }

    public int size(){
        return set.size();
    }

    public int indexOf(ReadablePacket r){
        return set.indexOf(r);
    }


    public ArrayList<ReadablePacket> getArrayList(){
        return set;
    }

    public boolean contains(ReadablePacket r){
        for(ReadablePacket r1:set){
            if(r1.getSequenceNumber() == r.getSequenceNumber())
                return true;
        }
        return false;
    }

    public void sortSet(){
        set.sort(new Comparator<ReadablePacket>(){

            @Override
            public int compare(ReadablePacket o1, ReadablePacket o2) {
                if(o1.getSequenceNumber() < o2.getSequenceNumber())
                    return -1;
                if(o1.getSequenceNumber() > o2.getSequenceNumber())
                    return 1;
                return 0;
            }
        });
    }


}