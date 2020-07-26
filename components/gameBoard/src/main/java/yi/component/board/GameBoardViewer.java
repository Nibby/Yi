package yi.component.board;

import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import yi.component.CanvasContainer;
import yi.component.Component;
import yi.core.go.EventListener;
import yi.core.go.GameModel;
import yi.core.go.NodeEvent;

import java.util.Stack;

/**
 * The core interface component that handles the display of the game board, as well as user input to browse
 * and edit {@link GameModel} data.
 */
public final class GameBoardViewer implements Component {

    private final CanvasContainer container;
    private final Stack<GameBoardCanvas> content = new Stack<>();

    private final GameBoardManager manager = new GameBoardManager();
    private GameModel gameModel;

    public GameBoardViewer() {
        this(new GameBoardSettings());
    }

    public GameBoardViewer(GameBoardSettings settings) {
        content.push(new GameBoardMainCanvas(manager));
        content.push(new GameBoardInputCanvas(manager));

        container = new CanvasContainer(content);
        container.addSizeUpdateListener(newSize -> {
            if (gameModel != null) {
                manager.onBoardSizeUpdate(newSize.getWidth(), newSize.getHeight(), gameModel);
                renderAll();
            }
        });

        applySettings(settings);
    }

    private void renderAll() {
        content.forEach(canvas -> canvas.render(manager));
    }

    private final EventListener<NodeEvent> currentMoveUpdateListener = (newCurrentNode) -> update();

    /**
     * Invoked when the game board should display a new game model.
     *
     * @param game The game model to subscribe to
     */
    public void setGameModel(GameModel game) {
        if (this.gameModel != null) {
            this.gameModel.onCurrentNodeUpdate().addListener(currentMoveUpdateListener);
        }
        this.gameModel = game;
        this.gameModel.onCurrentNodeUpdate().addListener(currentMoveUpdateListener);

        manager.onGameModelSet(game);
        content.forEach(canvas -> canvas.onGameModelSet(game, manager));
    }

    public void setEditable(boolean editable) {
        manager.edit.setEditable(editable);
    }

    /**
     * Invoked when there is an update to the {@link GameModel}.
     * To change the model used by this game board, call {@link #setGameModel(GameModel)} with
     * the new model.
     *
     */
    public void update() {
        manager.onGameUpdate(this.gameModel);
        content.forEach(canvas -> canvas.onGameUpdate(this.gameModel, this.manager));

        renderAll();
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

    @Override
    public Pane getComponent() {
        return container;
    }
}
