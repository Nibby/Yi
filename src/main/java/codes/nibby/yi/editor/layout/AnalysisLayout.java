package codes.nibby.yi.editor.layout;

import codes.nibby.yi.editor.GameEditorWindow;
import javafx.scene.layout.BorderPane;

/**
 * A perspective mainly designed for running AI reviews.
 *
 * @author Kevin Yang
 * Created on 29 August 2019
 */
public class AnalysisLayout extends AbstractLayout {

    public AnalysisLayout(GameEditorWindow editor) {
        super(editor);
    }

    @Override
    protected BorderPane createLayout() {
        return new BorderPane();
    }
}
