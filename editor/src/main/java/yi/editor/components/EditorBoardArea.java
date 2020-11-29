package yi.editor.components;

import javafx.scene.layout.*;
import org.jetbrains.annotations.Nullable;
import yi.component.board.GameBoardViewer;
import yi.core.go.GameModel;
import yi.core.go.GameNode;
import yi.editor.EditorMainMenuType;
import yi.editor.EditorTextResources;
import yi.editor.framework.accelerator.EditorAcceleratorId;
import yi.editor.framework.action.EditorAction;
import yi.editor.framework.action.EditorActionManager;
import yi.editor.framework.action.EditorToggleAction;
import yi.editor.settings.EditorSettings;

/**
 * Combines {@link yi.component.board.GameBoardViewer} with additional components
 * around the border edges while using the same background image to cover them all.
 */
public class EditorBoardArea extends BorderPane {

    private final GameBoardViewer board;
    private final EditorActionToolBar toolBar;

    public EditorBoardArea() {
        board = new GameBoardViewer();
        createBoardActions();
        EditorSettings.applySavedBoardSettings(board);

        toolBar = new EditorActionToolBar();
        toolBar.addSelectedToolChangeListener(newTool -> newTool.apply(board));

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

    private void createBoardActions() {
        var toggleCoordinates = new EditorToggleAction(EditorTextResources.MENUITEM_TOGGLE_COORDINATES, context -> {
            boolean nextState = !board.isShowingBoardCoordinates();
            board.setShowCoordinates(nextState);
        });
        toggleCoordinates.setInMainMenu(EditorMainMenuType.VIEW, 0.000d);
        toggleCoordinates.setAccelerator(EditorAcceleratorId.TOGGLE_BOARD_COORDINATES);
        board.addShowCoordinatesValueListener(toggleCoordinates::setSelected);
    }

    public void setGameModel(GameModel newModel) {
        newModel.getInfo().addChangeListener(toolBar::onGameInfoUpdate);
        board.setGameModel(newModel);
        toolBar.setGameModel(newModel);
    }

    public void setContentForLayout(EditorPerspective newLayout, GameModel gameModel) {
        toolBar.setContentForLayout(newLayout, gameModel);
    }

    public boolean requestUndo() {
        return board.requestUndo();
    }

    public boolean requestRedo() {
        return board.requestRedo();
    }

    public void onHighlightedNodeChange(@Nullable GameNode node) {
        board.setPreviewNode(node);
    }

    public GameBoardViewer getGameBoardViewer() {
        return board;
    }

    public boolean canUndo() {
        return board.canUndo();
    }

    public boolean canRedo() {
        return board.canRedo();
    }
}
