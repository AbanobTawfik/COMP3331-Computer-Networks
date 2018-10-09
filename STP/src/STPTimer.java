/**
 * This class will be a counting timer running on a seperate thread
 * it will run on both sender and receiver calculating time since the start of execution
 */
public class STPTimer extends Thread{
    //initialise the start time since run
    private long timeStarted = System.currentTimeMillis();
    //to calculate time passed we want to use this value to store
    //current time - time since started
    private long millisecondsElapsed = 0;
    //run the timer on a seperate thread
    @Override
    public void run() {
        new Thread(() -> {
            while (true) {
                //calculate the difference between start time and current time
                millisecondsElapsed = System.currentTimeMillis() - timeStarted;
                try{
                    //sleep for 100ms before next computation
                    Thread.sleep(100);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * This method will return the time since the start of execution
     *
     * @return the time since start of execution
     */
    public long timePassed()
    {
        //return the time elapsed since start of program
        return millisecondsElapsed;
    }

    /**
     * reset the timer to reset time elapsed
     */
    private void reset(){
        //reset the milliseconds elapsed
        this.millisecondsElapsed = 0;
        //set the start time to be the current system time
        timeStarted = System.currentTimeMillis();
    }
}