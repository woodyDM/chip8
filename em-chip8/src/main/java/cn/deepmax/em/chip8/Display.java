package cn.deepmax.em.chip8;

public interface Display {

    /**
     * draw to screen
     */
    void update();

    /**
     * clear all data
     */
    void clear();

    /**
     *
     * @param x
     * @param y
     * @param pixels
     * @return true if changed
     */
    boolean draw(int x, int y, short[] pixels);

}
