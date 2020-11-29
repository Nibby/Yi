package yi.component.board;

import javafx.scene.image.Image;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.common.BooleanPropertyListener;
import yi.common.NullablePropertyListener;
import yi.component.CanvasContainer;
import yi.component.YiComponent;
import yi.component.board.editmodes.AbstractEditMode;
import yi.core.go.EventListener;
import yi.core.go.GameModel;
import yi.core.go.GameNode;
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
    private final GameBoardInputCanvas inputCanvas;
    private final Stack<GameBoardCanvas> content = new Stack<>();

    private final GameBoardManager manager = new GameBoardManager();

    public GameBoardViewer() {
        inputCanvas = new GameBoardInputCanvas(manager);
        content.push(new GameBoardMainCanvas(manager));
        content.push(inputCanvas);

        container = new CanvasContainer(content);
        container.addSizeUpdateListener(newSize -> {
            manager.setBoardCanvasSize(newSize.getWidth(), newSize.getHeight(), manager.getGameModel());
            renderAll();
        });

        addRenderSignalHooks();
        setEditable(true);
    }

    private void addRenderSignalHooks() {
        manager.addPreviewNodeChangeListener(newPreview -> renderAll());
        manager.addShowCoordinateValueListener(newValue -> renderAll());
    }

    /**
     * Sets the action to execute when a list of files has been dragged onto the board
     * component.
     *
     * @param dragEventConsumer A function taking an input of list of files, and returns
     *                          a boolean result indicating whether the intended drag
     *                          action was successful or not.
     */
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
        if (manager.hasGameModel()) {
            content.forEach(canvas -> canvas.render(manager));
        }
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
        content.forEach(canvas -> canvas.onGameUpdate(manager.getGameModel(), this.manager));
    }

    /**
     * Requests that the previous edit be undone. If the undo operation is not available,
     * do nothing.
     *
     * @return Whether undo operation is available again after executing the undo request.
     */
    public boolean requestUndo() {
        if (manager.edit.canUndo()) {
            manager.edit.performUndo(manager);
        }
        return manager.edit.canUndo();
    }

    /**
     * Requests that the next edit be re-done. If the redo operation is not available,
     * do nothing.
     *
     * @return Whether redo operation is available again after executing the redo request.
     */
    public boolean requestRedo() {
        if (manager.edit.canRedo()) {
            manager.edit.performRedo(manager);
        }
        return manager.edit.canRedo();
    }

    /**
     * @return {@code true} if the game board is rendering in debug mode.
     */
    public boolean isDebugMode() {
        return manager.isDebugMode();
    }

    /**
     * Sets whether debug mode is active. In debug mode, additional items are
     * rendered on the screen for development purposes.
     *
     * @param debugMode {@code true} if debug mode is active.
     */
    public void setDebugMode(boolean debugMode) {
        this.manager.setDebugMode(debugMode);
    }

    /**
     * Sets the image to fill the entire region allocated for the game board.
     * May be null, in which case no image will be drawn.
     *
     * @param image Board image to draw.
     */
    public void setBoardImage(@Nullable Image image) {
        manager.view.boardImage = image;
    }

    /**
     * Sets the image to fill the entire canvas container with. May be null,
     * in which case no image will be drawn.
     *
     * @param image Background image to draw.
     */
    public void setBackgroundImage(@Nullable Image image) {
        manager.view.backgroundImage = image;
    }

    public @Nullable Image getBackgroundImage() {
        return manager.view.backgroundImage;
    }

    public void setGridColor(Color gridColor) {
        Objects.requireNonNull(gridColor, "gridColor must not be null");
        manager.view.boardGridColor = gridColor;
    }

    protected final GameBoardManager getManager() {
        return manager;
    }

    @Override
    public Pane getComponent() {
        return container;
    }

    /**
     * Add a listener to respond to coordinate label shown status.
     *
     * @param listener Listener to add.
     */
    public void addShowCoordinatesValueListener(BooleanPropertyListener listener) {
        manager.addShowCoordinateValueListener(listener);
    }

    /**
     * Sets whether or not to draw intersection co-ordinate labels on the game board.
     *
     * @param doShow {@code true} to draw coordinates.
     */
    public void setShowCoordinates(boolean doShow) {
        manager.setShowCoordinates(doShow);
    }

    /**
     * @return {@code true} if the game board is currently drawing coordinates.
     */
    public boolean isShowingBoardCoordinates() {
        return manager.isShowingCoordinates();
    }

    /**
     * Add a listener to respond to preview node value change.
     *
     * @param listener Listener to add.
     */
    public void addPreviewNodeChangeListener(NullablePropertyListener<GameNode> listener) {
        manager.addPreviewNodeChangeListener(listener);
    }

    /**
     * Set a {@link GameNode} as the preview node. If set, the game board will render the
     * position at that node rather than the current node. In addition, extra move labels
     * and markers will be rendered.
     * <p/>
     * Can be set to null to draw the current move instead.
     *
     * @param node Game node to set as preview, or {@code null} to draw the current node.
     *             The preview node should not be the current node.
     */
    public void setPreviewNode(@Nullable GameNode node) {
        manager.setPreviewNode(node);
    }

    /**
     * @return {@code true} if there exists at least one more edit that can be undone.
     */
    public boolean canUndo() {
        return manager.edit.canUndo();
    }

    /**
     * @return {@code true} if there exists at least one more edit that can be undone.
     */
    public boolean canRedo() {
        return manager.edit.canRedo();
    }
}
