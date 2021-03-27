package cn.deepmax.em.gui;

import cn.deepmax.em.chip8.BaseDisplay;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;

public class FxDisplay extends BaseDisplay {

    private Group group = new Group();
    private Rectangle[] rects = new Rectangle[LEN];
    static final int SCALE = 10;

    public FxDisplay() {
        init();
    }

    public Group getGroup() {
        return group;
    }

    private void init() {
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                Rectangle r = new Rectangle();
                r.setX(x *SCALE);
                r.setY(y *SCALE);
                r.setWidth(SCALE);
                r.setHeight(SCALE);
                group.getChildren().add(r);
                rects[get(x, y)] = r;
            }
        }
    }

    @Override
    protected void doUpdate() {

    }

    @Override
    public void update() {
        //to fresh ui
        Platform.runLater(()->{
            for (int i = 0; i < LEN; i++) {
                rects[i].setVisible(gfx[i]==1);
            }
        });

    }
}
