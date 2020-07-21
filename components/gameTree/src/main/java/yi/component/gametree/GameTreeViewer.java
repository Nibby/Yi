package yi.component.gametree;

import javafx.scene.Parent;
import yi.component.CanvasContainer;
import yi.component.Component;

/**
 * A component which displays the game model and its nodes as a tree graph.
 */
public final class GameTreeViewer implements Component {

    private final CanvasContainer component;
    private final GameTreeCanvas canvas;

    public GameTreeViewer() {
        canvas = new GameTreeCanvas();
        component = new CanvasContainer(canvas);
        component.addSizeUpdateListener(newSize -> canvas.render());
    }

    @Override
    public Parent getComponent() {
        return component;
    }
}
