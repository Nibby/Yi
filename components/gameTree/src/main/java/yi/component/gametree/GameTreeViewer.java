package yi.component.gametree;

import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import yi.component.CanvasContainer;
import yi.component.Component;
import yi.core.go.GoGameModel;

/**
 * A component which displays the game model and its nodes as a tree graph.
 */
public final class GameTreeViewer implements Component {

    private final CanvasContainer component;
    private final GameTreeCanvas canvas;
    private final Viewport viewport;

    private GoGameModel gameModel;
    private GameTreeStructure treeStructure;
    private long currentStateHash;

    public GameTreeViewer(GoGameModel gameModel) {
        viewport = new Viewport();
        canvas = new GameTreeCanvas();
        component = new CanvasContainer(canvas);
        component.addSizeUpdateListener(newSize -> updateViewportAndRender());

        setGameModel(gameModel);
    }

    public void update() {
        long stateHash = gameModel.getCurrentGameState().getStateHash();

        if (currentStateHash != stateHash) {
            treeStructure.update();
        }
        currentStateHash = stateHash;

        updateViewportAndRender();
    }

    private void updateViewportAndRender() {
        canvas.render(treeStructure.getElements());
    }

    public void setGameModel(@NotNull GoGameModel model) {
        this.gameModel = model;
        treeStructure = new GameTreeStructure(this.gameModel);
    }

    @Override
    public Parent getComponent() {
        return component;
    }
}
