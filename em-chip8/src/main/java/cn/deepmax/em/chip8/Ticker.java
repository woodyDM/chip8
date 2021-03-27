package cn.deepmax.em.chip8;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wudi
 * @date 2021/3/26
 */
public class Ticker {

    private static final long NANO = 1000_000_000;
    private static final AtomicInteger THREAD_INDEX = new AtomicInteger(0);
    private double fps;
    private final long perNano;
    private Thread thread;
    private RunnableWrapper task;
    private volatile boolean stop = false;
    private CountDownLatch latch;

    private static String getThreadName() {
        return "Ticker-Thread-" + THREAD_INDEX.getAndIncrement();
    }

    public Ticker(double runHz, Runnable task) {
        this.fps = runHz;
        this.perNano = (long) (NANO * 1.0 / runHz);
        setTask(task);
    }

    public Ticker(double runHz) {
        this.fps = runHz;
        this.perNano = (long) (NANO * 1.0 / runHz);
    }

    public void setTask(Runnable runnable) {
        this.task = new RunnableWrapper(Objects.requireNonNull(runnable));
        thread = new Thread(this.task);
        thread.setName(getThreadName());
        thread.setDaemon(true);
    }

    public void stop() {
        this.stop = true;
    }

    public void startAndWait() {
        this.latch = new CountDownLatch(1);
        start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            //ignore
        }

    }

    public String getFps() {
        BigDecimal d = new BigDecimal(task.getFps());
        BigDecimal n = d.setScale(1, RoundingMode.HALF_UP);
        return n.toString();
    }

    public void start() {
        if (task == null) {
            throw new IllegalStateException("no task added");
        }
        thread.start();
    }

    class RunnableWrapper implements Runnable {
        Runnable target;
        long lastTime;
        long thisTime;

        RunnableWrapper(Runnable target) {
            this.target = target;
        }

        double getFps() {
            if (lastTime == 0 || thisTime == 0) {
                return 0D;
            } else {
                return NANO * 1.0 / (thisTime - lastTime);
            }
        }

        @Override
        public void run() {

            long deadLine;
            long sleepTime;

            while (!stop) {
                lastTime = thisTime;
                thisTime = System.nanoTime();

                try {
                    target.run();
                } catch (Throwable t) {
                    System.err.println("Tick error: " + t.getMessage());
                    t.printStackTrace();
                }
                sleepTime = perNano - (System.nanoTime() - thisTime) - 1000_000;
                if (sleepTime > 0) {
                    try {
                        TimeUnit.NANOSECONDS.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        //ignore
                    }
                }
                deadLine = thisTime + perNano;
                while (System.nanoTime() < deadLine) {
                    System.nanoTime();
                }
            }
            if (latch != null) {
                latch.countDown();
            }
        }
    }

}
