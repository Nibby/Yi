package yi.editor.components;

import javafx.scene.layout.*;
import org.jetbrains.annotations.Nullable;
import yi.component.shared.audio.CommonAudioSets;
import yi.component.boardviewer.GameBoardViewer;
import yi.editor.framework.EditorComponent;
import yi.editor.framework.accelerator.EditorAcceleratorId;
import yi.editor.framework.action.EditorAction;
import yi.editor.framework.action.EditorActionManager;
import yi.editor.framework.action.EditorToggleAction;
import yi.editor.settings.EditorSettings;
import yi.core.go.GameModel;
import yi.core.go.GameNode;

import java.util.HashSet;
import java.util.Optional;

/**
 * Combines {@link yi.component.boardviewer.GameBoardViewer} with additional components
 * around the border edges while using the same background image to cover them all.
 */
public class EditorBoardArea implements EditorComponent<Pane> {

    private final BorderPane container;
    private final GameBoardViewer board;
    private final EditorActionToolBar toolBar;

    private final EditorToggleAction actionToggleCoordinates;

    public EditorBoardArea() {
        board = new GameBoardViewer();
        board.setAudio(CommonAudioSets.Stones.CERAMIC_BICONVEX);

        toolBar = new EditorActionToolBar();
        toolBar.addSelectedToolChangeListener(newTool -> newTool.apply(board));

        container = new BorderPane();
        container.setTop(toolBar);
        container.setCenter(board.getComponent());
        transferBoardBackgroundToContainer();

        // Individual scopes to create actions
        {
            actionToggleCoordinates = new EditorToggleAction(EditorTextResources.MENUITEM_TOGGLE_COORDINATES,
                    context -> {
                        boolean nextState = !board.isShowingBoardCoordinates();
                        board.setShowCoordinates(nextState);
                    }
            );
            actionToggleCoordinates.setInMainMenu(EditorMainMenuType.VIEW, 0.000d);
            actionToggleCoordinates.setAccelerator(EditorAcceleratorId.TOGGLE_BOARD_COORDINATES);
            board.addShowCoordinatesValueListener(actionToggleCoordinates::setSelected);
        }

        EditorSettings.applySavedBoardSettings(board);
    }

    private void transferBoardBackgroundToContainer() {
        var image = board.getBackgroundImage();
        if (image != null) {
            container.setBackground(new Background(new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                    new BackgroundSize(1.0d, 1.0d, true, true, false, true))));
            board.setBackgroundImage(null);
        }
    }

    public void setGameModel(GameModel newModel) {
        board.setGameModel(newModel);
    }

    public void setContentForLayout(EditorPerspective newLayout) {
        toolBar.setContentForLayout(newLayout);
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

    @Override
    public EditorAction[] getActions(EditorActionManager actionManager) {
        var actions = new HashSet<EditorAction>();
        actions.add(actionToggleCoordinates);
        actions.addAll(toolBar.getAllActions());

        return actions.toArray(new EditorAction[0]);
    }

    @Override
    public Optional<Pane> getComponent() {
        return Optional.of(container);
    }
}
