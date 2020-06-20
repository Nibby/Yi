package codes.nibby.yi.gui.board;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.util.Stack;

public final class GameBoard {

    private final BorderPane component;
    private final StackPane contentStackPane;
    private final Stack<GameBoardCanvas> content = new Stack<>();

    private final GameBoardManager manager = new GameBoardManager();

    public GameBoard() {
        content.push(new GameBoardGameCanvas());
        content.push(new GameBoardInputCanvas());

        contentStackPane = new StackPane();
        contentStackPane.getChildren().addAll(content);
        contentStackPane.widthProperty().addListener(newWidth -> this.updateSize());
        contentStackPane.heightProperty().addListener(newHeight -> this.updateSize());

        component = new BorderPane();
        component.setCenter(contentStackPane);
    }

    private void updateSize() {
        double boardWidth = contentStackPane.getWidth();
        double boardHeight = contentStackPane.getHeight();

        manager.onBoardSizeUpdate(boardWidth, boardHeight);

        content.forEach(canvas -> {
            canvas.setLayoutX(0);
            canvas.setLayoutY(0);
            canvas.setWidth(boardWidth);
            canvas.setHeight(boardHeight);

            canvas.render(manager);
        });
    }

    public Parent getComponent() {
        return component;
    }
}
