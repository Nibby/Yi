package yi.component.board;

import javafx.scene.image.Image;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.CanvasContainer;
import yi.component.YiComponent;
import yi.component.board.editmodes.AbstractEditMode;
import yi.core.go.EventListener;
import yi.core.go.GameModel;
import yi.core.go.NodeEvent;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Function;

/**
 * The core interface component that handles the display of the game board, as well as user
 * input to browse and edit {@link GameModel} data.
 */
public final class GameBoardViewer implements YiComponent {

    private final CanvasContainer container;
    private final GameBoardMainCanvas mainCanvas;
    private final GameBoardInputCanvas inputCanvas;
    private final Stack<GameBoardCanvas> content = new Stack<>();

    private final GameBoardManager manager = new GameBoardManager();

    public GameBoardViewer() {
        this(new GameBoardSettings());
    }

    public GameBoardViewer(GameBoardSettings settings) {
        mainCanvas = new GameBoardMainCanvas(manager);
        inputCanvas = new GameBoardInputCanvas(manager);
        content.push(mainCanvas);
        content.push(inputCanvas);

        container = new CanvasContainer(content);
        container.addSizeUpdateListener(newSize -> {
            if (manager.getGameModel() != null) {
                manager.setBoardCanvasSize(newSize.getWidth(), newSize.getHeight(), manager.getGameModel());
                renderAll();
            }
        });

        setEditable(true);
        applySettings(settings);
    }

    public void setDragAndDropBehaviour(Function<List<File>, Boolean> dragEventConsumer) {
        inputCanvas.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        inputCanvas.setOnDragDropped(event -> {
            var dragboard = event.getDragboard();
            var success = false;

            if (dragboard.hasFiles()) {
                var draggedFiles = dragboard.getFiles();
                success = dragEventConsumer.apply(draggedFiles);
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void renderAll() {
        content.forEach(canvas -> canvas.render(manager));
    }

    private final EventListener<NodeEvent> updateAllCanvas = (newCurrentNode) -> update();

    /**
     * Invoked when the game board should display a new game model.
     *
     * @param newModel The game model to subscribe to
     */
    public void setGameModel(GameModel newModel) {
        if (manager.hasGameModel()) {
            var currentModel = manager.getGameModel();

            currentModel.onCurrentNodeChange().removeListener(updateAllCanvas);
            currentModel.onCurrentNodeDataUpdate().removeListener(updateAllCanvas);
        }

        newModel.onCurrentNodeChange().addListener(updateAllCanvas);
        newModel.onCurrentNodeDataUpdate().addListener(updateAllCanvas);

        manager.setBoardCanvasSize(container.getWidth(), container.getHeight(), newModel);
        manager.setGameModel(newModel);
        content.forEach(canvas -> canvas.onGameModelSet(newModel, manager));

        renderAll();
    }

    /**
     * Set the edit method to apply when the board receives input events. In order for edits to be made,
     * the board viewer must be editable.
     *
     * @param editMode The edit method
     */
    public void setEditMode(@NotNull AbstractEditMode editMode) {
        Objects.requireNonNull(editMode, "Edit mode cannot be null. To disable editing, use setEditable(false)");

        manager.edit.setEditMode(editMode);
    }

    /**
     * Set whether the game model can be edited through the game board editor or not.
     *
     * @param editable true if editable
     */
    public void setEditable(boolean editable) {
        manager.edit.setEditable(editable);
    }

    /**
     * Invoked when there is an update to the {@link GameModel}.
     * To change the model used by this game board, call {@link #setGameModel(GameModel)} with
     * the new model.
     *
     */
    void update() {
        manager.onGameUpdate(manager.getGameModel());
        content.forEach(canvas -> canvas.onGameUpdate(manager.getGameModel(), this.manager));
    }

    public void requestUndo() {
        if (manager.edit.canUndo()) {
            manager.edit.performUndo(manager);
        }
    }

    public void requestRedo() {
        if (manager.edit.canRedo()) {
            manager.edit.performRedo(manager);
        }
    }

    public boolean isDebugMode() {
        return manager.isDebugMode();
    }

    public void setDebugMode(boolean debugMode) {
        this.manager.setDebugMode(debugMode);
    }

    public void applySettings(GameBoardSettings settings) {
        settings.getBoardImage().ifPresent(this::setBoardImage);
        settings.getGridColor().ifPresent(this::setGridColor);
        settings.getBackgroundImage().ifPresent(this::setBackgroundImage);
    }

    public void setBoardImage(@Nullable Image image) {
        manager.view.boardImage = image;
    }

    public void setBackgroundImage(@Nullable Image image) {
        manager.view.backgroundImage = image;
    }

    public Image getBackgroundImage() {
        return manager.view.backgroundImage;
    }

    public void setGridColor(Color gridColor) {
        manager.view.boardGridColor = gridColor;
    }

    GameBoardManager _getManager() {
        return manager;
    }

    @Override
    public Pane getComponent() {
        return container;
    }
}
