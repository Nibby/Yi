package yi.editor.components;

import javafx.scene.layout.*;
import yi.component.board.GameBoardViewer;

/**
 * Combines {@link yi.component.board.GameBoardViewer} with additional components
 * around the border edges while using the same background image to cover them all.
 */
public class GameBoardViewerComposite extends BorderPane {

    public GameBoardViewerComposite(GameBoardViewer viewer, EditorToolBar toolBar) {
        var image = viewer.getBackgroundImage();
        setBackground(new Background(new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(1.0d, 1.0d, true, true, false, true))));
        viewer.setBackgroundImage(null);

        setTop(toolBar);
        setCenter(viewer.getComponent());
    }
}
