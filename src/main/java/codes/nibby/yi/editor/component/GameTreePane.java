package codes.nibby.yi.editor.component;

import codes.nibby.yi.editor.GameEditorWindow;
import javafx.scene.layout.BorderPane;

/**
 * A component for displaying the game tree structure visually.
 * Allows user input to navigate.
 *
 * TODO: Implement this later.
 *
 * @author Kevin Yang
 * Created on 29 August 2019
 */
public class GameTreePane extends BorderPane {

    private GameEditorWindow editor;

    public GameTreePane(GameEditorWindow editor) {
        this.editor = editor;
        getStyleClass().add("editor_ui_gametree");
    }

}
