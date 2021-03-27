package cn.deepmax.em.chip8;

public class Timer {

    private int hz = 60;
    protected long perLoop = (long) (1E9 / hz);
    private long nextCountDown;
    private int counter;

    public void cycle() {
        if (counter > 0 && System.nanoTime() > nextCountDown) {
            counter--;
            nextCountDown += perLoop;
        }
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
        this.nextCountDown = System.nanoTime() + perLoop;
        onChange(counter);
    }

    protected void onChange(int counter) {

    }
}
