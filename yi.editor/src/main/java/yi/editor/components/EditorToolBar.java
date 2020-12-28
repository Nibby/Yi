package yi.editor.components;

import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import org.jetbrains.annotations.NotNull;
import yi.common.utilities.GuiUtilities;
import yi.models.go.GameModel;
import yi.models.go.GameModelInfo;

import static yi.editor.components.EditorTextResources.MOVE_COUNT;

public class EditorToolBar extends ToolBar {

    private final Label playerBlackName = new Label("", GuiUtilities.getIcon("/yi/editor/icons/blackStone_white@2x.png", getClass(), 16).orElse(null));
    private final Label playerBlackRank = new Label("");
    private final Label playerWhiteName = new Label("", GuiUtilities.getIcon("/yi/editor/icons/whiteStone_white@2x.png", getClass(), 16).orElse(null));
    private final Label playerWhiteRank = new Label("");

    private final Label moveLabel = new Label("");

    public EditorToolBar() {
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

        getItems().add(GuiUtilities.createStaticSpacer(4));
        getItems().add(playerBlackName);
        if (!playerBlackRank.getText().isBlank()) {
            getItems().add(playerBlackRank);
        }
        getItems().add(GuiUtilities.createStaticSpacer(8));

        getItems().add(playerWhiteName);
        if (!playerWhiteRank.getText().isBlank()) {
            getItems().add(playerWhiteRank);
        }

        getItems().add(GuiUtilities.createDynamicSpacer());

        getItems().add(moveLabel);
        getItems().add(GuiUtilities.createStaticSpacer(4));
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
}
