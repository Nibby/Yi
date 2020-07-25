package yi.component.gametree;

import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import yi.component.CanvasContainer;
import yi.component.Component;
import yi.core.common.EventListener;
import yi.core.common.NodeEvent;
import yi.core.go.GoGameModel;
import yi.core.go.GoGameStateUpdate;

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

    private void updateViewportAndRender() {
        canvas.render(treeStructure.getElements(), gameModel.getCurrentMove());
    }


    private final EventListener<NodeEvent<GoGameStateUpdate>> treeStructureChangeListener = (node) -> {
//        treeStructure.reconstruct();
//        updateViewportAndRender();
    };

    private final EventListener<NodeEvent<GoGameStateUpdate>> currentMoveChangeListener = (node) -> {
        treeStructure.reconstruct();
        updateViewportAndRender();
    };

    public void setGameModel(@NotNull GoGameModel model) {
        if (this.gameModel != null) {
            this.gameModel.onCurrentNodeUpdate().removeListener(currentMoveChangeListener);
            this.gameModel.onNodeAdd().removeListener(treeStructureChangeListener);
            this.gameModel.onNodeDelete().removeListener(treeStructureChangeListener);
        }

        this.gameModel = model;
        this.treeStructure = new GameTreeStructure(this.gameModel);

        this.gameModel.onCurrentNodeUpdate().addListener(currentMoveChangeListener);
        this.gameModel.onNodeAdd().addListener(treeStructureChangeListener);
        this.gameModel.onNodeDelete().addListener(treeStructureChangeListener);
    }

    @Override
    public Parent getComponent() {
        return component;
    }
}
