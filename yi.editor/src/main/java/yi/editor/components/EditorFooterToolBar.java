package yi.editor.components;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import org.jetbrains.annotations.NotNull;
import yi.component.shared.utilities.GuiUtilities;
import yi.core.go.GameModel;
import yi.core.go.GameModelInfo;
import yi.editor.framework.EditorTextResources;

import static yi.editor.framework.EditorTextResources.MOVE_COUNT;

public class EditorFooterToolBar extends ToolBar {

    private final Label playerBlackName = new Label("", GuiUtilities.getIcon("/yi/editor/icons/blackStone_white32.png", getClass(), 16).orElse(null));
    private final Label playerBlackRank = new Label("");
    private final Label playerWhiteName = new Label("", GuiUtilities.getIcon("/yi/editor/icons/whiteStone_white32.png", getClass(), 16).orElse(null));
    private final Label playerWhiteRank = new Label("");

    private final Button moveBack1 = new Button("", GuiUtilities.getIcon("/yi/editor/icons/arrowUp_white32.png", getClass(), 16).orElse(null));
    private final Button moveBack10 = new Button("", GuiUtilities.getIcon("/yi/editor/icons/arrowUpDouble_white32.png", getClass(), 16).orElse(null));
    private final Button moveBackToBeginning = new Button("", GuiUtilities.getIcon("/yi/editor/icons/arrowUpmost_white32.png", getClass(), 16).orElse(null));

    private final Button moveNext1 = new Button("", GuiUtilities.getIcon("/yi/editor/icons/arrowDown_white32.png", getClass(), 16).orElse(null));
    private final Button moveNext10 = new Button("", GuiUtilities.getIcon("/yi/editor/icons/arrowDownDouble_white32.png", getClass(), 16).orElse(null));
    private final Button moveToEnd = new Button("", GuiUtilities.getIcon("/yi/editor/icons/arrowDownmost_white32.png", getClass(), 16).orElse(null));

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

        moveBack1.getStyleClass().add("button-style3");
        moveBack10.getStyleClass().add("button-style3");
        moveBackToBeginning.getStyleClass().add("button-style3");
        moveNext1.getStyleClass().add("button-style3");
        moveNext10.getStyleClass().add("button-style3");
        moveToEnd.getStyleClass().add("button-style3");

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
                moveBackToBeginning,
                moveBack10,
                moveBack1,
                moveLabel,
                moveNext1,
                moveNext10,
                moveToEnd,
                GuiUtilities.createStaticSpacer(8)
        );
    }
}
