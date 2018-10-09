/**
 * This class will be used to run the program, it uses STPReceiver as a wrapper
 * to call the functionallity from there allowing a simple to follow and debug proccess
 */
public class Receiver {
    /**
     * this method main, allows us to run the program through the terminal
     *
     * @param args program arguements passed through
     */
    public static void main(String args[]){
        //if we are not supplied correct number of arguements, we want to ouput usage message
        if(args.length != 2){
            System.out.println("usage: <port number> <file_r.pdf>");
            System.exit(-1);
        }
        //otherwise we create a bootstrap for our receiver
        Receiver receiver = new Receiver();
        //call operate which runs the program
        receiver.operate(args);
    }

    /**
     * This method is a wrapper that initialises the STPReceiver
     * and then calls operate from within the class
     *
     * @param args program arguements
     */
    public void operate(String args[]){
        STPReceiver receiver = new STPReceiver(args);
        receiver.operate();
    }
}
