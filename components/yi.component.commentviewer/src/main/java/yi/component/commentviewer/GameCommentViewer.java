package yi.component.commentviewer;

import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;
import yi.component.shared.component.YiComponent;
import yi.core.go.EventListener;
import yi.core.go.GameModel;
import yi.core.go.GameNode;
import yi.core.go.NodeEvent;

import java.util.Objects;

public final class GameCommentViewer implements YiComponent {

    private final BorderPane container;
    private final TextArea commentEditor = new TextArea();

    private GameModel gameModel = null;
    private GameNode nodeToShow = null;
    private final EventListener<NodeEvent> currentMoveListener = event -> setText(event.getNode());

    public GameCommentViewer() {
        container = new BorderPane();
        container.setCenter(commentEditor);
        commentEditor.getStyleClass().add("editor-comment-viewer");
        commentEditor.getStyleClass().add("fg-dark-secondary");
        commentEditor.setWrapText(true);

        commentEditor.textProperty().addListener((observable, oldValue, newValue) -> {
            if (commentEditor.isEditable()) {
                saveCommentsToNode();
            }
        });
    }

    private void saveCommentsToNode() {
        String text = commentEditor.getText();
        gameModel.getEditor().setCommentOnCurrentNode(text);
    }

    public void setGameModel(@NotNull GameModel gameModel) {
        if (this.gameModel != null) {
            this.gameModel.onCurrentNodeChange().removeListener(currentMoveListener);
        }
        this.gameModel = Objects.requireNonNull(gameModel);
        gameModel.onCurrentNodeChange().addListener(currentMoveListener);
        setText(gameModel.getCurrentNode());
    }

    public void setText(@NotNull GameNode node) {
        Objects.requireNonNull(node, "Node cannot be null");

        // Prevent setting the same text for the same node which resets the caret position
        if (nodeToShow == null || !nodeToShow.equals(node)) {
            nodeToShow = node;
            commentEditor.setText(node.getComments());
        }
    }

    public void setEditable(boolean isEditable) {
        commentEditor.setEditable(isEditable);
    }

    public Pane getComponent() {
        return container;
    }

}
