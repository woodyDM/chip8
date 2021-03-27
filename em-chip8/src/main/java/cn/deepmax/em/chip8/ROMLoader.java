package cn.deepmax.em.chip8;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ROMLoader {

    public static short[] loadRomFromFile(File file) throws IOException {
        Path path = Paths.get(file.getAbsolutePath());
        byte[] bytes = Files.readAllBytes(path);
        short[] program = new short[bytes.length];

        for (int i=0; i < bytes.length; i++) {
            // Prevent signed integer extension when casting
            program[i] = (short)(0x000000FF & (int)bytes[i]);
        }

        return program;
    }
}
