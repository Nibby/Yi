package codes.nibby.yi.app.components;

import codes.nibby.yi.app.framework.AppAccelerator;
import codes.nibby.yi.app.framework.GlobalHelper;
import codes.nibby.yi.app.framework.AppText;
import codes.nibby.yi.app.framework.action.AppAction;
import codes.nibby.yi.app.framework.action.AppActionManager;
import codes.nibby.yi.app.framework.action.AppToggleAction;
import codes.nibby.yi.app.settings.AppSettings;
import javafx.scene.layout.*;
import org.jetbrains.annotations.Nullable;
import codes.nibby.yi.app.components.board.GameBoardViewer;
import codes.nibby.yi.app.audio.CommonAudioSets;
import codes.nibby.yi.models.GameModel;
import codes.nibby.yi.models.GameNode;

import java.util.HashSet;
import java.util.Optional;

/**
 * Combines {@link GameBoardViewer} with additional components
 * around the border edges while using the same background image to cover them all.
 */
public final class AppBoardArea implements AppComponent<Pane> {

    private final BorderPane container;
    private final GameBoardViewer board;
    private final AppEditToolsToolBar toolBar;

    private final AppToggleAction actionToggleCoordinates;

    public AppBoardArea() {
        board = new GameBoardViewer();
        if (!GlobalHelper.isRunningAsTest()) {
            board.setAudio(CommonAudioSets.Stones.CERAMIC_BICONVEX);
        }

        toolBar = new AppEditToolsToolBar();
        toolBar.addSelectedToolChangeListener(newTool -> newTool.apply(board));

        container = new BorderPane();
        container.setTop(toolBar);
        container.setCenter(board.getComponent());
        transferBoardBackgroundToContainer();

        actionToggleCoordinates = new AppToggleAction(
                AppText.MENUITEM_TOGGLE_COORDINATES,
                context -> {
                    boolean nextState = !board.isShowingBoardCoordinates();
                    board.setShowCoordinates(nextState);
                }
        );
        actionToggleCoordinates.setInMenuBar(AppMainMenuType.VIEW, 0.000d);
        actionToggleCoordinates.setAccelerator(AppAccelerator.TOGGLE_BOARD_COORDINATES);
        board.addShowCoordinatesValueListener(actionToggleCoordinates::setSelected);

        AppSettings.applySavedBoardSettings(board);
    }

    private void transferBoardBackgroundToContainer() {
        var image = board.getBackgroundImage();
        if (image != null) {
            container.setBackground(
                new Background(
                    new BackgroundImage(
                        image,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(
                            1.0d,
                            1.0d,
                            true, // width as percentage
                            true, // height as percentage
                            false, // contain
                            true // cover
                        )
                    )
                )
            );
            board.setBackgroundImage(null);
        }
    }

    public void setGameModel(GameModel newModel) {
        board.setGameModel(newModel);
    }

    public void setContentForPerspective(AppPerspective newLayout) {
        toolBar.setContentForLayout(newLayout);
    }

    public void onHighlightedNodeChange(@Nullable GameNode node) {
        board.setPreviewNode(node);
    }

    public GameBoardViewer getGameBoardViewer() {
        return board;
    }

    @Override
    public AppAction[] getActions(AppActionManager actionManager) {
        var actions = new HashSet<AppAction>();
        actions.add(actionToggleCoordinates);
        actions.addAll(toolBar.getAllActions());

        return actions.toArray(new AppAction[0]);
    }

    @Override
    public Optional<Pane> getComponent() {
        return Optional.of(container);
    }
}
