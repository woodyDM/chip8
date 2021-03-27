package cn.deepmax.em.chip8;

public abstract class BaseDisplay implements Display {
    //width 64 , height 32 pixels
    protected byte[] gfx = new byte[LEN];
    public static final int LEN = 64 * 32;
    private final int fps = 60;
    private long nextNano = System.nanoTime();
    private final long perNano = (long) 1E9 / fps;

    @Override
    public void clear() {
        for (int i = 0; i < LEN; i++) {
            gfx[i] = 0;
        }
    }

    @Override
    public void update() {
        long cur = System.nanoTime();
        if (cur > nextNano) {
            doUpdate();
            nextNano += perNano;
        }
    }

    protected abstract void doUpdate();


    /**
     * // Draw sprite of width 8 and height N at (V[x], V[y]) (N bytes)
     * draw(Vx,Vy,N)
     * Draws a sprite at coordinate (VX, VY) that has a width of 8
     * pixels and a height of N+1 pixels. Each row of 8 pixels is read as bit-coded
     * starting from memory location I; I value doesn’t change after the execution of
     * this instruction. As described above, VF is set
     * to 1 if any screen pixels are flipped from
     * set to unset when the sprite is drawn, and to 0 if that doesn’t happen
     */
    @Override
    public boolean draw(int x, int y, short[] pixels) {
        boolean fliped = false;
        int bit;
        for (int j = 0; j < pixels.length; j++) {
            short px = pixels[j];
            for (int i = 0; i < 8; i++) {
                //get px binary pos at i
                //eg: 0b11100001
                bit = ((px >> (7 - i))) & 1;
                //collide
                if (bit == 1) {
                    int idx = get(x + i, y + j);
                    byte b = gfx[idx];
                    if (b == 1) {
                        gfx[idx] = 0;
                        fliped = true;
                    } else {
                        gfx[idx] = 1;
                    }
                }
            }
        }
        return fliped;
    }

    protected int get(int x, int y) {
        int i = (x % 64) + (y % 32) * 64;
        if (i < 0 || i >= LEN) {
            throw new IllegalStateException("invalid x,y at (" + x + ", " + y + ")");
        }
        return i;
    }
}
