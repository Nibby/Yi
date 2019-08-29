package codes.nibby.yi.editor.perspective;

import codes.nibby.yi.editor.GameEditorWindow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class SimplePerspectiveLayout extends PerspectiveLayout {

    public SimplePerspectiveLayout(GameEditorWindow editor) {
        super(editor);
    }

    @Override
    protected Pane createLayout() {
        BorderPane content = new BorderPane();
        content.setCenter(getEditor().getGameBoard());
        content.setTop(getEditor().getToolBar());
        return content;
    }
}
