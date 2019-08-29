package codes.nibby.yi.editor.layout;

import codes.nibby.yi.editor.GameEditorWindow;
import javafx.scene.layout.BorderPane;

public class PresenterLayout extends AbstractLayout {

    public PresenterLayout(GameEditorWindow editor) {
        super(editor);
    }

    @Override
    protected BorderPane createLayout() {
        return new BorderPane();
    }
}
