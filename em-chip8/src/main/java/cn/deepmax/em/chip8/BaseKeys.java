package cn.deepmax.em.chip8;

public class BaseKeys implements Keys {

    protected byte[] keys = new byte[16];


    @Override
    public boolean isPress(int i) {
        return false;
    }

    @Override
    public int waitPress() {
        return 0;
    }
}
