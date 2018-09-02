public class STPTimer extends Thread{
    private long timeStarted = System.currentTimeMillis();
    private long millisecondsElapsed = 0;

    @Override
    public void run() {
        new Thread(() -> {
            //infinitely generating boards with a delay
            while (true) {
                millisecondsElapsed += System.currentTimeMillis() - timeStarted;
            }
        }).start();
    }

    public long timePassed(){
        return millisecondsElapsed;
    }

    private void reset(){
        this.millisecondsElapsed = 0;
        timeStarted = System.currentTimeMillis();
    }
}