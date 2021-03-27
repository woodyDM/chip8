package cn.deepmax.em.chip8;

import java.util.Arrays;
import java.util.Random;

public class CPU {
    private static final int MEM_START = 0x200;
    private static final int MEM_FONT_START = 0x000;
    //use short for 8-bit bytes , and int for 16-bit shorts
    private short[] ram = new short[4096];
    //8-bit register :
    private short[] V = new short[16];
    //16-bit register:
    private int I;
    //program counter
    private short pc = MEM_START;
    //stack
    private short[] stack = new short[16];
    //stack pointer  0-16 ,next  stack[] array index
    private byte sp;

    //display and timer
    private Keys keys;
    private Display display;
    private Timer delayTimer = new Timer();
    private Timer soundTimer = new Timer();
    private final Random random = new Random();

    public CPU(short[] rom, Keys keys, Display display) {
        this.keys = keys;
        this.display = display;

        loadFontAndRom(rom);
    }

    private void loadFontAndRom(short[] rom) {
        //load program
        int loader = MEM_START;
        for (short b : rom) {
            ram[loader++] = b;
        }
        //load font
        short[] fontMap = {
                0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
                0x20, 0x60, 0x20, 0x20, 0x70, // 1
                0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
                0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
                0x90, 0x90, 0xF0, 0x10, 0x10, // 4
                0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
                0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
                0xF0, 0x10, 0x20, 0x40, 0x40, // 7
                0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
                0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
                0xF0, 0x90, 0xF0, 0x90, 0x90, // A
                0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
                0xF0, 0x80, 0x80, 0x80, 0xF0, // C
                0xE0, 0x90, 0x90, 0x90, 0xE0, // D
                0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
                0xF0, 0x80, 0xF0, 0x80, 0x80  // F
        };
        int fontLoader = MEM_FONT_START;
        for (short b : fontMap) {
            ram[fontLoader++] = b;
        }
    }

    /**
     * @return false if can't continue
     */
    public boolean cpuCycle() {
        delayTimer.cycle();
        soundTimer.cycle();

        int lastPc = pc;
        int opcode = ram[pc] << 8 | ram[pc + 1];

        int high = opcode & 0xF000;
        switch (high) {
            case 0x0000:
                switch (opcode) {
                    case 0x00E0:
                        op_00E0(opcode);
                        break;
                    case 0x00EE:
                        op_00EE(opcode);
                        break;
                    default:
                        System.out.println("Unsupported code " + opcode);
                        next();
                        break;
                }
                break;
            case 0x1000:
                op_1NNN(opcode);
                break;
            case 0x2000:
                op_2NNN(opcode);
                break;
            case 0x3000:
                op_3XNN(opcode);
                break;
            case 0x4000:
                op_4XNN(opcode);
                break;
            case 0x5000:
                op_5XY0(opcode);
                break;
            case 0x6000:
                op_6XNN(opcode);
                break;
            case 0x7000:
                op_7XNN(opcode);
                break;
            case 0x8000: {
                switch (opcode & 0x000F) {
                    case 0x0000:
                        op_8XY0(opcode);
                        break;
                    case 0x0001:
                        op_8XY1(opcode);
                        break;
                    case 0x0002:
                        op_8XY2(opcode);
                        break;
                    case 0x0003:
                        op_8XY3(opcode);
                        break;
                    case 0x0004:
                        op_8XY4(opcode);
                        break;
                    case 0x0005:
                        op_8XY5(opcode);
                        break;
                    case 0x0006:
                        op_8XY6(opcode);
                        break;
                    case 0x0007:
                        op_8XY7(opcode);
                        break;
                    case 0x000E:
                        op_8XYE(opcode);
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
                break;
            }

            case 0x9000:
                op_9XY0(opcode);
                break;
            case 0xA000:
                op_ANNN(opcode);
                break;
            case 0xB000:
                op_BNNN(opcode);
                break;
            case 0xC000:
                op_CXNN(opcode);
                break;
            case 0xD000:
                op_DXYN(opcode);
                break;
            case 0xE000:
                switch (opcode & 0xFF) {
                    case 0x9E:
                        op_EX9E(opcode);
                        break;
                    case 0xA1:
                        op_EXA1(opcode);
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
                break;
            case 0xF000:
                switch (opcode & 0xFF) {
                    case 0x07:
                        op_FX07(opcode);
                        break;
                    case 0x0A:
                        op_FX0A(opcode);
                        break;
                    case 0x15:
                        op_FX15(opcode);
                        break;
                    case 0x18:
                        op_FX18(opcode);
                        break;
                    case 0x1E:
                        op_FX1E(opcode);
                        break;
                    case 0x29:
                        op_FX29(opcode);
                        break;
                    case 0x33:
                        op_FX33(opcode);
                        break;
                    case 0x55:
                        op_FX55(opcode);
                        break;
                    case 0x65:
                        op_FX65(opcode);
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }

        if (lastPc == pc) {
            System.out.println("PC stacked " + instrToString(opcode) + " ,program exit");
            return false;
        }
        return true;
    }

    public static String instrToString(int instr) {
        return String.format("0x%04X", instr);
    }

    //Fills V0 to VX (including VX) with values from memory starting at address I.
    // The offset from I is increased by 1 for each value written, but I itself is left unmodified.[d]
    private void op_FX65(int opcode) {
        int x = getX(opcode);
        for (int i = 0; i <= x; i++) {
            V[i] = ram[I + i];
        }
        next();
    }

    //reg_dump(Vx,&I)	Stores V0 to VX (including VX) in memory starting at address I.
    // The offset from I is increased by 1 for each value written, but I itself is left unmodified.[d]
    private void op_FX55(int opcode) {
        int x = getX(opcode);
        for (int i = 0; i <= x; i++) {
            ram[I + i] = V[i];
        }
        next();
    }

    /**
     * set_BCD(Vx);
     * *(I+0)=BCD(3);
     * <p>
     * *(I+1)=BCD(2);
     * <p>
     * *(I+2)=BCD(1);
     * <p>
     * Stores the binary-coded decimal representation of VX,
     * with the most significant of three digits at the address in I,
     * the middle digit at I plus 1, and the least significant digit at I plus 2.
     * (In other words, take the decimal representation of VX, place the hundreds digit in memory at location in I,
     * the tens digit at location I+1, and the ones digit at location I+2.)
     *
     * @param opcode
     */
    private void op_FX33(int opcode) {
        int num = V[getX(opcode)];
        int hundreds = num / 100;
        int tens = (num / 10) % 10;
        int n = num % 10;
        ram[I] = (short) hundreds;
        ram[I + 1] = (short) tens;
        ram[I + 2] = (short) n;
        next();
    }

    //I=sprite_addr[Vx]	Sets I to the location of the sprite for the character in VX.
    // Characters 0-F (in hexadecimal) are represented by a 4x5 font.
    private void op_FX29(int opcode) {
        short character = V[getX(opcode)];
        if (character < 0 || character > 0xF) {
            throw new IllegalStateException("invalid char  " + character);
        }
        I = MEM_FONT_START + character * 5;
        next();
    }

    //I +=Vx	Adds VX to I. VF is not affected.[c]
    private void op_FX1E(int opcode) {
        I += V[getX(opcode)];
        I &= 0xFFF;
        next();
    }

    //sound_timer(Vx)	Sets the sound timer to VX.
    private void op_FX18(int opcode) {
        soundTimer.setCounter(V[getX(opcode)]);
        next();
    }

    //delay_timer(Vx)	Sets the delay timer to VX.
    private void op_FX15(int opcode) {
        delayTimer.setCounter(V[getX(opcode)]);
        next();
    }

    //Vx = get_key()	A key press is awaited, and then stored in VX. (Blocking Operation. All instruction halted until ne
    private void op_FX0A(int opcode) {
        V[getX(opcode)] = (short) keys.waitPress();
        next();
    }

    //Vx = get_delay()	Sets VX to the value of the delay timer.
    private void op_FX07(int opcode) {
        V[getX(opcode)] = (short) delayTimer.getCounter();
        next();
    }


    //if(key()!=Vx)	Skips the next instruction if the key stored in VX isn't pressed.
    // (Usually the next instruction is a jump to skip a code block)
    private void op_EXA1(int opcode) {
        if (!keys.isPress(V[getX(opcode)])) {
            next();
        }
        next();
    }

    //if(key()==Vx)	Skips the next instruction if the key stored in VX is pressed.
    // (Usually the next instruction is a jump to skip a code block)
    private void op_EX9E(int opcode) {
        if (keys.isPress(V[getX(opcode)])) {
            next();
        }
        next();
    }

    /**
     * draw(Vx,Vy,N)
     * Draws a sprite at coordinate (VX, VY) that has a width of 8
     * pixels and a height of N+1 pixels. Each row of 8 pixels is read as bit-coded
     * starting from memory location I; I value doesn’t change after the execution of
     * this instruction. As described above, VF is set
     * to 1 if any screen pixels are flipped from
     * set to unset when the sprite is drawn, and to 0 if that doesn’t happen
     *
     * @param opcode
     */

    private void op_DXYN(int opcode) {
        int x = V[getX(opcode)];
        int y = V[getY(opcode)];
        int n = getN(opcode);
        short[] pixels = Arrays.copyOfRange(ram, I, I + n);
        boolean changed = display.draw(x, y, pixels);
        V[0xF] = (short) (changed ? 1 : 0);
        next();
    }


    //Vx=rand()&NN	Sets VX to the result of a bitwise and operation on a random number (Typically: 0 to 255) and NN.
    private void op_CXNN(int opcode) {
        int r = random.nextInt(256);
        V[getX(opcode)] = (short) (r & getNN(opcode));
        next();
    }


    //PC=V0+NNN	Jumps to the address NNN plus V0.
    private void op_BNNN(int opcode) {
        pc = (short) (V[0] + getNNN(opcode));
    }

    //I = NNN	Sets I to the address NNN.
    private void op_ANNN(int opcode) {
        I = getNNN(opcode);
        next();
    }


    //if(Vx!=Vy)	Skips the next instruction if VX doesn't equal VY. (Usually the next instruction is a jump to skip a code block)
    private void op_9XY0(int opcode) {
        if (V[getX(opcode)] != V[getY(opcode)]) {
            next();
        }
        next();
    }

    //Vx<<=1	Stores the most significant bit of VX in VF and then shifts VX to the left by 1.[b]
    private void op_8XYE(int opcode) {
        int x = getX(opcode);
        V[0xF] = (short) (V[x] >> 7);
        V[x] = (short) (V[x] << 1 & 0xFF);

        next();
    }

    //Vx=Vy-Vx	Sets VX to VY minus VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
    private void op_8XY7(int opcode) {
        int x = getX(opcode);
        int y = getY(opcode);
        if (V[y] < V[x]) { //borrow
            V[0xF] = 0;
            V[x] = (short) (0x100 + V[y] - V[x]);
        } else {
            V[0xF] = 1;
            V[x] = (short) (V[y] - V[x]);
        }
        next();
    }

    //Vx>>=1	Stores the least significant bit of VX in VF and then shifts VX to the right by 1.[b]
    private void op_8XY6(int opcode) {
        int x = getX(opcode);
        V[0xF] = (short) (V[x] & 0x1);
        V[x] >>>= 1;
        next();
    }

    //Vx -= Vy VY is subtracted from VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
    private void op_8XY5(int opcode) {
        int y = getY(opcode);
        int x = getX(opcode);

        if (V[y] > V[x]) { // borrow!
            V[0xF] = 0;
            V[x] += 0x100 - V[y];
        } else {
            V[0xF] = 1;
            V[x] -= V[y];
        }

        next();
        assert V[x] < 0x100;
        assert V[x] >= 0x0;
    }

    //Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't.
    private void op_8XY4(int opcode) {
        int sum = V[getX(opcode)] + V[getY(opcode)];
        V[0xF] = (short) (sum > 0xFF ? 1 : 0);
        V[getX(opcode)] = (short) (sum % 0x100);
        next();
    }

    //Sets VX to VX xor VY.
    private void op_8XY3(int opcode) {
        V[getX(opcode)] ^= V[getY(opcode)];
        next();
    }

    //Sets VX to VX and VY. (Bitwise AND operation)
    private void op_8XY2(int opcode) {
        V[getX(opcode)] &= V[getY(opcode)];
        next();
    }

    //Sets VX to VX or VY. (Bitwise OR operation)
    private void op_8XY1(int opcode) {
        V[getX(opcode)] |= V[getY(opcode)];
        next();
    }


    //Sets VX to the value of VY.
    private void op_8XY0(int opcode) {
        V[getX(opcode)] = V[getY(opcode)];
        next();
    }

    //Adds NN to VX. (Carry flag is not changed)
    private void op_7XNN(int opcode) {
        int x = getX(opcode);
        V[x] += getNN(opcode);
        V[x] %= 0x100;
        next();
    }


    //Sets VX to NN.
    private void op_6XNN(int opcode) {
        V[getX(opcode)] = getNN(opcode);
        next();
    }

    //Skips the next instruction if VX equals VY. (Usually the next instruction is a jump to skip a code block)
    private void op_5XY0(int opcode) {
        if (V[getX(opcode)] == V[getY(opcode)]) {
            next();
        }
        next();
    }

    //Skips the next instruction if VX doesn't equal NN. (Usually the next instruction is a jump to skip a code block)
    private void op_4XNN(int opcode) {
        if (V[getX(opcode)] != getNN(opcode)) {
            next();
        }
        next();
    }

    //Skips the next instruction if VX equals NN. (Usually the next instruction is a jump to skip a code block)
    private void op_3XNN(int opcode) {
        if (V[getX(opcode)] == getNN(opcode)) {
            next();
        }
        next();
    }


    //Calls subroutine at NNN.
    private void op_2NNN(int opcode) {
        stack[sp] = pc;
        sp++;
        pc = getNNN(opcode);
    }

    //Jumps to address NNN.
    private void op_1NNN(int opcode) {
        pc = getNNN(opcode);
    }

    //Returns from a subroutine.
    private void op_00EE(int opcode) {
        if (sp < 1) {
            throw new IllegalStateException("0xOOEE: invalid stack depth " + sp);
        }
        pc = stack[sp - 1];
        stack[sp - 1] = 0;
        sp--;
        next();
    }

    //Clears the screen.
    private void op_00E0(int code) {
        display.clear();
        next();
    }


    //  ------------------------- util methods -------------------------
    private void next() {
        pc += 2;
    }

    private int getX(int opcode) {
        return (opcode & 0x0F00) >> 8;
    }

    private int getY(int opcode) {
        return (opcode & 0x00F0) >> 4;
    }

    private short getN(int opcode) {
        return (short) (opcode & 0x000F);
    }

    private short getNN(int opcode) {
        return (short) (opcode & 0x00FF);
    }

    private short getNNN(int opcode) {
        return (short) (opcode & 0x0FFF);
    }

}
