package codes.nibby.yi.editor.gui.board;

import codes.nibby.yi.go.GoGameModel;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

import java.util.Stack;

/**
 * The core interface component that handles the display of the game board, as well as user input to browse
 * and edit {@link GoGameModel} data.
 */
public final class GameBoard {

    private final Pane component;
    private final Stack<GameBoardCanvas> content = new Stack<>();

    private final GameBoardManager manager = new GameBoardManager();
    private GoGameModel gameModel;

    public GameBoard() {
        content.push(new GameBoardMainCanvas(manager));
        content.push(new GameBoardInputCanvas(manager));

        component = new Pane() {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();

                if (gameModel != null) {
                    final double x = snappedLeftInset();
                    final double y = snappedTopInset();
                    final double w = snapSizeX(getWidth()) - x - snappedRightInset();
                    final double h = snapSizeY(getHeight()) - y - snappedBottomInset();

                    manager.onBoardSizeUpdate(w, h, gameModel);

                    content.forEach(canvas -> {
                        canvas.setLayoutX(x);
                        canvas.setLayoutY(y);
                        canvas.setWidth(w);
                        canvas.setHeight(h);
                    });

                    renderAll();
                }
            }
        };
        component.getChildren().addAll(content);
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

    /**
     *
     * @return The JavaFx container used to display the game board
     */
    public Parent getComponent() {
        return component;
    }
}
