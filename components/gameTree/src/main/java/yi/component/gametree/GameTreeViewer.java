package yi.component.gametree;

import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import yi.component.CanvasContainer;
import yi.component.Component;
import yi.core.common.EventListener;
import yi.core.common.GameNode;
import yi.core.common.NodeEvent;
import yi.core.go.GoGameModel;
import yi.core.go.GoGameStateUpdate;

/**
 * A component which displays the game model and its nodes as a tree graph.
 */
public final class GameTreeViewer implements Component {

    private final CanvasContainer component;
    private final GameTreeCanvas canvas;
    private final Camera camera;

    private GoGameModel gameModel;
    private GameTreeStructure treeStructure;
    private final GameTreeElementSize elementSize;

    public GameTreeViewer(GoGameModel gameModel) {
        canvas = new GameTreeCanvas();
        component = new CanvasContainer(canvas);
        elementSize = new GameTreeElementSize();
        camera = new Camera(component.getWidth(), component.getHeight());
        component.addSizeUpdateListener(newSize -> {
            camera.setViewportSize(newSize.getWidth(), newSize.getHeight());
            render();
        });
        camera.addPanAnimationListener(this::render);

        setGameModel(gameModel);
        updateCameraAndRender(gameModel.getCurrentMove());
    }

    private void updateCameraAndRender(GameNode<GoGameStateUpdate> currentNode) {
        treeStructure.getElements().stream()
                .filter(element -> (element instanceof TreeNodeElement) && ((TreeNodeElement) element).getNode().equals(currentNode))
                .findAny()
                .ifPresent(currentNodeElement -> camera.setCenterElementWithAnimation(currentNodeElement, elementSize.getGridSize()));

        render();
    }

    private void render() {
        canvas.render(camera, treeStructure.getElements(), gameModel.getCurrentMove(), elementSize);
    }

    private final EventListener<NodeEvent<GoGameStateUpdate>> treeStructureChangeListener = (event) -> {
        treeStructure.reconstruct();
        render();
    };

    private final EventListener<NodeEvent<GoGameStateUpdate>> currentMoveChangeListener = (event) -> updateCameraAndRender(event.getNode());

    public void setGameModel(@NotNull GoGameModel model) {
        if (this.gameModel != null) {
            this.gameModel.onCurrentNodeUpdate().removeListener(currentMoveChangeListener);
            this.gameModel.onNodeAdd().removeListener(treeStructureChangeListener);
            this.gameModel.onNodeDelete().removeListener(treeStructureChangeListener);
        }

        this.gameModel = model;
        this.treeStructure = new GameTreeStructure(this.gameModel);
        this.camera.reset();

        this.gameModel.onCurrentNodeUpdate().addListener(currentMoveChangeListener);
        this.gameModel.onNodeAdd().addListener(treeStructureChangeListener);
        this.gameModel.onNodeDelete().addListener(treeStructureChangeListener);
    }

    @Override
    public Parent getComponent() {
        return component;
    }
}
