package codes.nibby.yi.editor.component;

import codes.nibby.yi.config.Config;
import codes.nibby.yi.editor.GameEditorWindow;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameListener;
import codes.nibby.yi.game.GameNode;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

import java.util.ResourceBundle;

/**
 * A component that displays the comment text at each game node.
 * <p>
 * TODO: Implement later
 *
 * @author Kevin Yang
 * Created on 29 August 2019
 */
public class MoveCommentPane extends BorderPane implements GameListener {

    private GameEditorWindow editor;
    private TextArea textArea;

    public MoveCommentPane(GameEditorWindow editor) {
        this.editor = editor;
        this.textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setOnKeyTyped(e -> {
            Game game = editor.getGame();
            GameNode node = game.getCurrentNode();
            node.setComments(textArea.getText());
        });
        ResourceBundle bundle = Config.getLanguage().getResourceBundle("GameEditorWindow");
        textArea.setPromptText(bundle.getString("editor.sidebar.comments.no_comment"));
        textArea.getStyleClass().add("editor_node_comments");
        setCenter(textArea);

    }

    @Override
    public void gameInitialized(Game game) {
        textArea.setText(game.getCurrentNode().getComments());
    }

    @Override
    public void gameNodeUpdated(GameNode currentMove, boolean newMove) {
        textArea.setText(currentMove.getComments());
    }

    @Override
    public void gameModified(Game game) {
    }
}
