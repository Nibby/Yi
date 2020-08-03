package yi.component.board;

import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import yi.component.CanvasContainer;
import yi.component.Component;
import yi.component.board.editmodes.AbstractEditMode;
import yi.core.go.EventListener;
import yi.core.go.GameModel;
import yi.core.go.NodeEvent;

import java.util.Objects;
import java.util.Stack;

/**
 * The core interface component that handles the display of the game board, as well as user input to browse
 * and edit {@link GameModel} data.
 */
public final class GameBoardViewer implements Component {

    private final CanvasContainer container;
    private final GameBoardMainCanvas mainCanvas;
    private final GameBoardInputCanvas inputCanvas;
    private final Stack<GameBoardCanvas> content = new Stack<>();

    private final GameBoardManager manager = new GameBoardManager();
    private GameModel gameModel;

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
            if (gameModel != null) {
                manager.setBoardCanvasSize(newSize.getWidth(), newSize.getHeight(), gameModel);
                renderAll();
            }
        });

        setEditable(true);
        applySettings(settings);
    }

    private void renderAll() {
        content.forEach(canvas -> canvas.render(manager));
    }

    private final EventListener<NodeEvent> updateAllCanvas = (newCurrentNode) -> update();

    /**
     * Invoked when the game board should display a new game model.
     *
     * @param game The game model to subscribe to
     */
    public void setGameModel(GameModel game) {
        if (this.gameModel != null) {
            this.gameModel.onCurrentNodeChange().removeListener(updateAllCanvas);
            this.gameModel.onCurrentNodeDataUpdate().removeListener(updateAllCanvas);
        }

        this.gameModel = game;

        this.gameModel.onCurrentNodeChange().addListener(updateAllCanvas);
        this.gameModel.onCurrentNodeDataUpdate().addListener(updateAllCanvas);

        manager.setBoardCanvasSize(container.getWidth(), container.getHeight(), game);
        manager.setGameModel(game);
        content.forEach(canvas -> canvas.onGameModelSet(game, manager));
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
        manager.onGameUpdate(this.gameModel);
        content.forEach(canvas -> canvas.onGameUpdate(this.gameModel, this.manager));

        renderAll();
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

    public void setBoardImage(Image image) {
        manager.view.boardImage = image;
    }

    public void setBackgroundImage(Image image) {
        manager.view.backgroundImage = image;
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
