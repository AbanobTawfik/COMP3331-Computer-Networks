/**
 * This class will be used to run the program, it uses STPSender as a wrapper
 * to call the functionallity from there allowing a simple to follow and debug proccess
 */
public class Sender {
    /**
     * this method main, allows us to run the program through the terminal
     *
     * @param args program arguements passed through
     */
    public static void main(String args[]){
        //if we are not supplied correct number of arguements, we want to ouput usage message
        if(args.length != 14){
            System.out.println("usage: <receiver_host_ip> <receiver_port> <file.pdf> <Naximum Window Size>" +
                    "<Maximum Segment Size> <Gamma> <pDrop> <pDuplicate> <pCorrupt> <pOrder> <maxOrder> <pDelay>" +
                    "<maxDelay> <seed>");
            System.exit(-1);
        }
        //otherwise we create a bootstrap for our sender
        Sender sender = new Sender();
        //call operate which runs the program
        sender.operate(args);
    }

    /**
     * This method is a wrapper that initialises the STPSender
     * and then calls operate from within the class
     *
     * @param args program arguements
     */
    public void operate(String args[]){
        STPSender sender = new STPSender(args);
        sender.operate();
    }
}