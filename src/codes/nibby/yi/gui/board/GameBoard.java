package codes.nibby.yi.gui.board;

import codes.nibby.yi.model.GoGame;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

import java.util.Stack;

public final class GameBoard {

    private final Pane component;
    private final Stack<GameBoardCanvas> content = new Stack<>();

    private final GameBoardManager manager = new GameBoardManager();

    public GameBoard() {
        content.push(new GameBoardMainCanvas());
        content.push(new GameBoardInputCanvas());

        component = new Pane() {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();

                final double x = snappedLeftInset();
                final double y = snappedTopInset();
                final double w = snapSizeX(getWidth()) - x - snappedRightInset();
                final double h = snapSizeY(getHeight()) - y - snappedBottomInset();

                manager.onBoardSizeUpdate(w, h);

                content.forEach(canvas -> {
                    canvas.setLayoutX(x);
                    canvas.setLayoutY(y);
                    canvas.setWidth(w);
                    canvas.setHeight(h);
                });

                renderAll();
            }
        };
        component.getChildren().addAll(content);
    }

    private void renderAll() {
        content.forEach(canvas -> canvas.render(manager));
    }

    public void initialize(GoGame game) {
        manager.onGameInitialize(game);
    }

    public void update(GoGame game) {
        manager.onGameUpdate(game);
    }

    public Parent getComponent() {
        return component;
    }
}
