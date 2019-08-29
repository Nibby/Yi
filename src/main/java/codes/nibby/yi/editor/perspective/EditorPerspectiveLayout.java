package codes.nibby.yi.editor.perspective;

import codes.nibby.yi.editor.GameEditorWindow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class EditorPerspectiveLayout extends PerspectiveLayout {

    public EditorPerspectiveLayout(GameEditorWindow editor) {
        super(editor);
    }

    @Override
    protected Pane createLayout() {
        BorderPane content = new BorderPane();
        content.setTop(getEditor().getToolBar());
        return content;
    }
}
