package codes.nibby.yi.app.components.comment;

import codes.nibby.yi.app.framework.YiComponent;
import codes.nibby.yi.app.framework.YiStyleClass;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;
import codes.nibby.yi.models.EventListener;
import codes.nibby.yi.models.GameModel;
import codes.nibby.yi.models.GameNode;
import codes.nibby.yi.models.NodeEvent;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

public final class GameCommentViewer implements YiComponent {

    private final BorderPane container;
    private final TextArea commentEditor = new TextArea();

    private GameModel gameModel = null;
    private GameNode nodeToShow = null;
    private final EventListener<NodeEvent> currentMoveListener = event -> setCommentText(event.getNode());
    private final Map<GameNode, Integer> cachedCaretPositionsByNode = new WeakHashMap<>();

    public GameCommentViewer() {
        container = new BorderPane();
        container.setCenter(commentEditor);
        commentEditor.getStyleClass().addAll(
            YiStyleClass.BACKGROUND_DARK_SECONDARY.getName(),
            YiStyleClass.BACKGROUND_RADIUS_0.getName(),
            YiStyleClass.BORDER_INSETS_0.getName(),
            YiStyleClass.FOREGROUND_DARK.getName(),
            YiStyleClass.FONT_SIZE_14.getName(),
            YiStyleClass.DARK_SCROLL_PANE_CONTAINER.getName()
        );
        commentEditor.setWrapText(true);
        commentEditor.caretPositionProperty().addListener((evt, oldValue, newValue) -> {
            if (nodeToShow != null) {
                saveCaretPosition(nodeToShow);
            }
        });

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
        setCommentText(gameModel.getCurrentNode());
    }

    public void setCommentText(@NotNull GameNode node) {
        Objects.requireNonNull(node, "Node cannot be null");

        // Prevent setting the same text for the same node which resets the caret position
        if (nodeToShow == null || !nodeToShow.equals(node)) {
            nodeToShow = node;
            commentEditor.setText(node.getComments());
        }
        if (nodeToShow != null && commentEditor.isEditable()) {
            restoreCaretPosition(nodeToShow, node.getComments());
        }
    }

    public void setEditable(boolean isEditable) {
        if (!isEditable && nodeToShow != null) {
            saveCaretPosition(nodeToShow);
        }
        commentEditor.setEditable(isEditable);
    }

    private void saveCaretPosition(@NotNull GameNode node) {
        Objects.requireNonNull(node);
        cachedCaretPositionsByNode.put(node, commentEditor.getCaretPosition());
    }

    private void restoreCaretPosition(GameNode node, String text) {
        if (node != null) {
            int cachedPosition = cachedCaretPositionsByNode.getOrDefault(node, text.length() - 1);
            commentEditor.positionCaret(cachedPosition);
        }
    }

    public Pane getComponent() {
        return container;
    }

}
