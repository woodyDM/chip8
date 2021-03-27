package cn.deepmax.em.gui;

import cn.deepmax.em.chip8.Timer;

public class SoundTimer extends Timer {
    @Override
    public void onChange(int count) {
        SoundUtils.playSine(1000, (int) (perLoop / 1e6) * count);
    }
}
