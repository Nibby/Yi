package codes.nibby.yi.editor.gui.board;

import codes.nibby.yi.go.GoGameModel;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

import java.util.Stack;

public final class GameBoard {

    private final Pane component;
    private final Stack<GameBoardCanvas> content = new Stack<>();

    private final GameBoardManager manager = new GameBoardManager();
    private GoGameModel gameModel;

    public GameBoard() {
        content.push(new GameBoardMainCanvas());
        content.push(new GameBoardInputCanvas());

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

    public void initialize(GoGameModel game) {
        this.gameModel = game;
        manager.onGameInitialize(game);
    }

    public void update(GoGameModel game) {
        manager.onGameUpdate(game);
    }

    public Parent getComponent() {
        return component;
    }
}
