package cn.deepmax.em.gui;

import cn.deepmax.em.chip8.CPU;
import cn.deepmax.em.chip8.ROMLoader;
import cn.deepmax.em.chip8.Ticker;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * the chip8 entry class
 */
public class Chip8 extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Alert alert = showTips();
        alert.showAndWait();

        File file = chooseRom(primaryStage);
        if (file == null) {
            System.exit(0);
        }

        FxDisplay display = new FxDisplay();
        FxKeyboard board = new FxKeyboard(primaryStage);

        Group root = display.getGroup();
        Scene scene = new Scene(root, 640, 320);

        scene.setOnKeyPressed(board.getPressHandler());
        scene.setOnKeyReleased(board.getReleaseHandler());

        short[] rom = ROMLoader.loadRomFromFile(file);
        CPU cpu = new CPU(rom, board, display,new SoundTimer());

        Ticker displayTicker = new Ticker(60, display::update);
        Ticker cpuTicker = new Ticker(512);
        cpuTicker.setTask(()->{
            boolean goon = cpu.cpuCycle();
            if (!goon) {
                cpuTicker.stop();
                displayTicker.stop();
                showClose("Game reach end");
            }
            Platform.runLater(()->{
                String title = String.format("CPU: %sHZ,UI: %sHz", cpuTicker.getFps(), displayTicker.getFps());
                primaryStage.setTitle(title);
            });
        });
        cpuTicker.setOnError(t->{
            showClose("Error occurs");
            throw new RuntimeException(t);
        });

        primaryStage.setResizable(false);
        primaryStage.setTitle("Chip8");
        primaryStage.setScene(scene);
        primaryStage.show();

        displayTicker.start();
        cpuTicker.start();
    }

    private File getDir() {
        String path = System.getProperty("user.dir");
        if (path == null || path.equals("")) {
            return new File("");
        }
        return new File(path);
    }

    private File chooseRom(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open chip8 rom");
        fileChooser.setInitialDirectory(getDir());

        return fileChooser.showOpenDialog(primaryStage);
    }

    private Alert showTips() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chip8");
        alert.setHeaderText("Choose chip8 rom and continue");

        alert.setContentText("---------   ---------\n" +
                " 1 2 3 C     1 2 3 4\n" +
                " 4 5 6 D     Q W E R\n" +
                " 7 8 9 E     A S D F\n" +
                " A 0 B F     Z X C V");
        return alert;
    }

    private void showClose(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Chip8");
            alert.setHeaderText(message);
            alert.showAndWait();
            System.exit(0);
        });
    }



}
