public class Sender {
    public static void main(String args[]){
        if(args.length != 14){
            System.out.println("usage: <receiver_host_ip> <receiver_port> <file.pdf> <Naximum Window Size>" +
                    "<Maximum Segment Size> <Gamma> <pDrop> <pDuplicate> <pCorrupt> <pOrder> <maxOrder> <pDelay>" +
                    "<maxDelay> <seed>");
            System.exit(1);
        }
        Sender sender = new Sender();
        sender.operate(args);
    }

    public void operate(String args[]){
        STPSender sender = new STPSender(args);
        sender.operate();
    }
}