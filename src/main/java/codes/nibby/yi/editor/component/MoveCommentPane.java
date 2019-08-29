package codes.nibby.yi.editor.component;

import codes.nibby.yi.editor.GameEditorWindow;
import javafx.scene.layout.Pane;

/**
 * A component that displays the comment text at each game node.
 *
 * TODO: Implement later
 *
 * @author Kevin Yang
 * Created on 29 August 2019
 */
public class MoveCommentPane extends Pane {

    private GameEditorWindow editor;

    public MoveCommentPane(GameEditorWindow editor) {
        this.editor = editor;
        getStyleClass().add("editor_ui_comments");
    }

}
