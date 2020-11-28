package yi.editor.components;

import javafx.scene.layout.*;
import org.jetbrains.annotations.Nullable;
import yi.component.board.GameBoardViewer;
import yi.core.go.GameModel;
import yi.core.go.GameNode;
import yi.editor.settings.Settings;

/**
 * Combines {@link yi.component.board.GameBoardViewer} with additional components
 * around the border edges while using the same background image to cover them all.
 */
public class EditorBoardArea extends BorderPane {

    private final GameBoardViewer board;
    private final EditorActionToolBar toolBar;

    public EditorBoardArea() {
        board = new GameBoardViewer();
        Settings.applySavedBoardSettings(board);

        toolBar = new EditorActionToolBar();
        toolBar.addToolSelectionListener(newTool -> newTool.apply(board));

        setTop(toolBar);
        setCenter(board.getComponent());
        transferBoardBackgroundToSelf();
    }

    private void transferBoardBackgroundToSelf() {
        var image = board.getBackgroundImage();
        if (image != null) {
            setBackground(new Background(new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                    new BackgroundSize(1.0d, 1.0d, true, true, false, true))));
            board.setBackgroundImage(null);
        }
    }

    public void setGameModel(GameModel newModel) {
        newModel.getInfo().addChangeListener(toolBar::onGameInfoUpdate);
        board.setGameModel(newModel);
        toolBar.setGameModel(newModel);
    }

    public void setContentForLayout(ContentLayout newLayout, GameModel gameModel) {
        toolBar.setContentForLayout(newLayout, gameModel);
    }

    public void requestUndo() {
        board.requestUndo();
    }

    public void requestRedo() {
        board.requestRedo();
    }

    public void onHighlightedNodeChange(@Nullable GameNode node) {
        board.setPreviewNode(node);
    }

    public GameBoardViewer getGameBoardViewer() {
        return board;
    }
}
