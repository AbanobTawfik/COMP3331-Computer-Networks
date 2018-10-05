import java.util.ArrayList;
import java.util.Comparator;

public class PacketSet {
    private ArrayList<ReadablePacket> set = new ArrayList<ReadablePacket>();

    public void add(ReadablePacket r){
        if(set.contains(r)){
            return;
        }else{
            set.add(r);
        }
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


    public void sortSet(){
        set.sort(new Comparator<ReadablePacket>(){

            @Override
            public int compare(ReadablePacket o1, ReadablePacket o2) {
                if(o1.getSequenceNumber() < o2.getSequenceNumber())
                    return 1;
                if(o1.getSequenceNumber() > o2.getSequenceNumber())
                    return -1;
                return 0;
            }
        });
    }


}
