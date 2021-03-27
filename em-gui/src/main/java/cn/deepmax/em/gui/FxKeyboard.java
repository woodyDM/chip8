package cn.deepmax.em.gui;

import cn.deepmax.em.chip8.Keys;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FxKeyboard implements Keys {
    private Stage primaryStage;
    private byte[] keys = new byte[0x10];
    private Press pressHandler = new Press();
    private Release releaseHandler = new Release();
    private Lock lock = new ReentrantLock();
    private Condition pressCondition = lock.newCondition();
    private final AtomicInteger lastPressKey = new AtomicInteger(0);

    public FxKeyboard(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @Override
    public boolean isPress(int i) {
        return keys[i] == 1;
    }

    /**
     * called by cpu thread
     *
     * @return
     */
    @Override
    public int waitPress() {
        try {
            lock.lock();
            //  -1 means waiting press
            lastPressKey.set(-1);
            Platform.runLater(()->primaryStage.setTitle("Waiting key press ... "));
            pressCondition.awaitUninterruptibly();
            return lastPressKey.get();
        } finally {
            lock.unlock();
        }
    }


    public class Press implements EventHandler<KeyEvent> {
        @Override
        public void handle(KeyEvent event) {
            int idx = keymap(event.getCode());
            if (idx != -1) {
                keys[idx] = 1;
                //if some one is waiting for press
                if (lastPressKey.get() == -1) {
                    try {
                        lock.lock();
                        if (lastPressKey.get() == -1) {
                            lastPressKey.set(idx);
                            pressCondition.signalAll();
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }
        }
    }

    public class Release implements EventHandler<KeyEvent> {
        @Override
        public void handle(KeyEvent event) {
            int idx = keymap(event.getCode());
            if (idx != -1) {
                keys[idx] = 0;
            }
        }
    }

    public Press getPressHandler() {
        return pressHandler;
    }

    public Release getReleaseHandler() {
        return releaseHandler;
    }

    private int keymap(KeyCode k) {
        switch (k) {
            case DIGIT1:
                return 0x1;
            case DIGIT2:
                return 0x2;
            case DIGIT3:
                return 0x3;
            case DIGIT4:
                return 0xc;

            case Q:
                return 0x4;
            case W:
                return 0x5;
            case E:
                return 0x6;
            case R:
                return 0xd;

            case A:
                return 0x7;
            case S:
                return 0x8;
            case D:
                return 0x9;
            case F:
                return 0xe;

            case Z:
                return 0xa;
            case X:
                return 0x0;
            case C:
                return 0xb;
            case V:
                return 0xf;
            default:
                return -1;
        }

    }
}
