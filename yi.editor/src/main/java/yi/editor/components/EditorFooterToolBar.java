package yi.editor.components;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import org.jetbrains.annotations.NotNull;
import yi.component.shared.i18n.TextResource;
import yi.component.shared.utilities.GuiUtilities;
import yi.component.shared.utilities.IconUtilities;
import yi.core.go.GameModel;
import yi.core.go.GameModelInfo;
import yi.editor.EditorWindow;
import yi.editor.framework.EditorTextResources;
import yi.editor.framework.action.EditorAction;
import yi.editor.framework.action.EditorActionManager;
import yi.editor.framework.action.EditorBasicAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static yi.editor.framework.EditorTextResources.MOVE_COUNT;

public class EditorFooterToolBar extends ToolBar implements EditorComponent<ToolBar> {

    private final Label playerBlackName = new Label("", IconUtilities.loadIcon("/yi/editor/icons/blackStone_white32.png", getClass(), 16).orElse(null));
    private final Label playerBlackRank = new Label("");
    private final Label playerWhiteName = new Label("", IconUtilities.loadIcon("/yi/editor/icons/whiteStone_white32.png", getClass(), 16).orElse(null));
    private final Label playerWhiteRank = new Label("");

    private final List<EditorAction> navActions = new ArrayList<>();
    private final EditorAction toPrevious1Action = createNavAction(
            EditorTextResources.TO_PREVIOUS_NODE,
            GameModel::toPreviousNode,
            "/yi/editor/icons/arrowUp_white32.png"
    );

    private final EditorAction toPrevious10Action = createNavAction(
            EditorTextResources.TO_PREVIOUS_10_NODES,
            model -> model.toPreviousNode(10),
            "/yi/editor/icons/arrowUpDouble_white32.png"
    );

    private final EditorAction toRootAction = createNavAction(
            EditorTextResources.TO_ROOT_NODE,
            model -> model.setCurrentNode(model.getRootNode()),
            "/yi/editor/icons/arrowUpmost_white32.png"
    );

    private final EditorAction toNext1Action = createNavAction(
            EditorTextResources.TO_NEXT_NODE,
            GameModel::toNextNode,
            "/yi/editor/icons/arrowDown_white32.png"
    );

    private final EditorAction toNext10Action = createNavAction(
            EditorTextResources.TO_NEXT_10_NODES,
            model -> model.toNextNode(10),
            "/yi/editor/icons/arrowDownDouble_white32.png"
    );

    private final EditorAction toVariationEndAction = createNavAction(
            EditorTextResources.TO_VARIATION_END,
            model -> model.toNextNode(Integer.MAX_VALUE),
            "/yi/editor/icons/arrowDownmost_white32.png"
    );

    private final Label moveLabel = new Label("");

    public EditorFooterToolBar() {
        // TODO: Consider putting these CSS class strings into a constant class ...
        playerBlackName.getStyleClass().add("editor-player-name-hud-label");
        playerBlackName.setMaxWidth(160);
        playerBlackRank.getStyleClass().add("editor-player-rank-hud-label");
        playerBlackRank.setMaxWidth(50);
        playerWhiteName.getStyleClass().add("editor-player-name-hud-label");
        playerWhiteName.setMaxWidth(160);
        playerWhiteRank.getStyleClass().add("editor-player-rank-hud-label");
        playerWhiteRank.setMaxWidth(50);

        moveLabel.getStyleClass().add("editor-move-number-hud-label");
        getStyleClass().add("editor-player-info-toolbar");
    }

    private void updateGameModelInfo(GameModel gameModel) {
        var blackName = gameModel.getInfo().getPlayerBlackName();
        playerBlackName.setText(blackName.isBlank()
                ? EditorTextResources.DEFAULT_BLACK_NAME.getLocalisedText() : blackName);

        var blackRank = gameModel.getInfo().getPlayerBlackRank();
        playerBlackRank.setVisible(!blackRank.isBlank());
        playerBlackRank.setManaged(!blackRank.isBlank());
        playerBlackRank.setText(gameModel.getInfo().getPlayerBlackRank());

        var whiteName = gameModel.getInfo().getPlayerWhiteName();
        playerWhiteName.setText(whiteName.isBlank()
                ? EditorTextResources.DEFAULT_WHITE_NAME.getLocalisedText() : whiteName);

        var whiteRank = gameModel.getInfo().getPlayerWhiteRank();
        playerWhiteRank.setVisible(!whiteRank.isBlank());
        playerWhiteRank.setManaged(!whiteRank.isBlank());
        playerWhiteRank.setText(gameModel.getInfo().getPlayerWhiteRank());

        updateMoveInfo(gameModel.getCurrentMoveNumber());
    }

    private void updateMoveInfo(int moveNumber) {
        String moveText = MOVE_COUNT.getLocalisedText(moveNumber);
        moveLabel.setText(moveText);
    }

    public void setGameModel(@NotNull GameModel newModel) {
        newModel.getInfo().addChangeListener(this::onGameInfoUpdate);
        newModel.onCurrentNodeChange().addListener(newValue -> updateMoveInfo(newValue.getNode().getMoveNumber()));

        updateGameModelInfo(newModel);
    }

    public void onGameInfoUpdate(final String key, Object newValue) {
        switch (key) {
            case GameModelInfo.KEY_PLAYER_BLACK_NAME:
                playerBlackName.setText(newValue.toString());
                break;
            case GameModelInfo.KEY_PLAYER_BLACK_RANK:
                playerBlackRank.setText(newValue.toString());
                break;
            case GameModelInfo.KEY_PLAYER_WHITE_NAME:
                playerWhiteName.setText(newValue.toString());
                break;
            case GameModelInfo.KEY_PLAYER_WHITE_RANK:
                playerWhiteRank.setText(newValue.toString());
                break;
        }
    }

    public void setContentForPerspective(EditorPerspective perspective) {
        getItems().clear();
        getItems().addAll(
                GuiUtilities.createStaticSpacer(8),
                playerBlackName,
                playerBlackRank,
                GuiUtilities.createStaticSpacer(8),
                playerWhiteName,
                playerWhiteRank,
                GuiUtilities.createDynamicSpacer(),
                toRootAction.getAsComponent(),
                toPrevious10Action.getAsComponent(),
                toPrevious1Action.getAsComponent(),
                moveLabel,
                toNext1Action.getAsComponent(),
                toNext10Action.getAsComponent(),
                toVariationEndAction.getAsComponent(),
                GuiUtilities.createStaticSpacer(8)
        );
    }

    private EditorAction createNavAction(TextResource text, Consumer<GameModel> action, String iconPath) {
        var actionItem = new EditorBasicAction(text, context -> {
            EditorWindow window = context.getEditorWindow();
            GameModel model = window.getGameModel();
            action.accept(model);
        });
        actionItem.setIcon(IconUtilities.loadIcon(iconPath, EditorFooterToolBar.class, 16).orElse(null));
        actionItem.setComponentCompact(true);

        Node button = actionItem.getAsComponent();
        assert button != null;
        button.setFocusTraversable(false);
        button.getStyleClass().add("button-style3");

        navActions.add(actionItem);

        return actionItem;
    }

    @Override
    public @NotNull EditorAction[] getActions(EditorActionManager actionManager) {
        return navActions.toArray(new EditorAction[0]);
    }

    @Override
    public Optional<ToolBar> getComponent() {
        return Optional.of(this);
    }
}
