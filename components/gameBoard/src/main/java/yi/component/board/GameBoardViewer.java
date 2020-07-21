package yi.component.board;

import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import yi.component.CanvasContainer;
import yi.component.Component;
import yi.core.go.GoGameModel;

import java.util.Stack;

/**
 * The core interface component that handles the display of the game board, as well as user input to browse
 * and edit {@link GoGameModel} data.
 */
public final class GameBoardViewer implements Component {

    private final CanvasContainer container;
    private final Stack<GameBoardCanvas> content = new Stack<>();

    private final GameBoardManager manager = new GameBoardManager();
    private GoGameModel gameModel;

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

    /**
     * Invoked when the game board should display a new game model.
     *
     * @param game The game model to subscribe to
     */
    public void setModel(GoGameModel game) {
        this.gameModel = game;

        manager.onGameModelSet(game);
        content.forEach(canvas -> canvas.onGameModelSet(game, manager));
    }

    public void setEditable(boolean editable) {
        manager.edit.setEditable(editable);
    }

    /**
     * Invoked when there is an update to the {@link GoGameModel}. The model must
     * be identical to the model that was last {@link #setModel(GoGameModel) initialized}.
     * <p/>
     * To change the model used by this game board, call {@link #setModel(GoGameModel)} with
     * the new model.
     *
     * @param game The last game model used to initialize the game board
     * @throws IllegalArgumentException If {@param game} is not the last model used to initialize the game board.
     */
    public void update(GoGameModel game) {
        if (this.gameModel != game) {
            throw new IllegalArgumentException("Unrecognised game model");
        }

        manager.onGameUpdate(game);
        content.forEach(canvas -> canvas.onGameUpdate(game, manager));

        renderAll();
    }

    public void applySettings(GameBoardSettings settings) {
        settings.getBackgroundImage().ifPresent(this::setBackgroundImage);
        settings.getGridColor().ifPresent(this::setGridColor);
    }

    public void setBackgroundImage(Image image) {
        manager.view.boardImage = image;
    }

    public void setGridColor(Color gridColor) {
        manager.view.boardGridColor = gridColor;
    }

    @Override
    public Parent getComponent() {
        return container;
    }
}
