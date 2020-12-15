package yi.component.commentview;

import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;
import yi.common.component.YiComponent;
import yi.models.go.EventListener;
import yi.models.go.GameModel;
import yi.models.go.GameNode;
import yi.models.go.NodeEvent;

import java.util.Objects;

public final class GameCommentViewer implements YiComponent {

    private final BorderPane container;
    private final TextArea commentEditor = new TextArea();

    private GameModel gameModel = null;
    private final EventListener<NodeEvent> currentMoveListener = event -> setCommentsFromNode(event.getNode());

    public GameCommentViewer() {
        container = new BorderPane();
        container.setCenter(commentEditor);
        commentEditor.getStyleClass().add("editor-comment-viewer");
        commentEditor.getStyleClass().add("fg-dark-secondary");
        commentEditor.setWrapText(true);

        commentEditor.textProperty().addListener(event -> {
            String text = commentEditor.getText();
            gameModel.setCommentOnCurrentNode(text);
        });
    }

    private void setCommentsFromNode(GameNode node) {
        this.setText(node.getComments());
    }

    public void setGameModel(@NotNull GameModel gameModel) {
        if (this.gameModel != null) {
            this.gameModel.onCurrentNodeChange().removeListener(currentMoveListener);
        }
        this.gameModel = Objects.requireNonNull(gameModel);
        gameModel.onCurrentNodeChange().addListener(currentMoveListener);
        setCommentsFromNode(gameModel.getCurrentNode());
    }

    public void setText(@NotNull String text) {
        Objects.requireNonNull(text, "Text cannot be null, use an empty string instead");
        commentEditor.setText(text);
    }

    public Pane getComponent() {
        return container;
    }

}
