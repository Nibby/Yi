package yi.editor.components;

import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.common.Property;
import yi.common.PropertyListener;
import yi.common.utilities.GuiUtilities;
import yi.common.component.YiToggleButton;
import yi.editor.EditorTextResources;
import yi.editor.EditorTool;
import yi.editor.EditorWindow;
import yi.editor.framework.action.EditorAction;
import yi.editor.framework.action.EditorToggleAction;
import yi.models.go.GameModel;
import yi.models.go.GameModelInfo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static yi.editor.EditorTextResources.MOVE_COUNT;

/**
 * Primary toolbar for {@link EditorWindow} that displays a set of supported editing tools
 * and other options.
 */
public class EditorActionToolBar extends ToolBar {

    private final Property<EditorTool> selectedTool = new Property<>(EditorTool.PLAY_MOVE);

    private final ToggleGroup toolButtonGroup;
    private final ToggleGroup toolMenuGroup;

    private YiToggleButton toolPlayMove; // Effectively final and non-null because of assertions in constructor
    private final List<EditorToggleAction> editToolActions;

    private final Label playerBlackName = new Label("", GuiUtilities.getIcon("/icons/blackStone/blackStone_white@2x.png", getClass(), 16).orElse(null));
    private final Label playerBlackRank = new Label("");
    private final Label playerWhiteName = new Label("", GuiUtilities.getIcon("/icons/whiteStone/whiteStone_white@2x.png", getClass(), 16).orElse(null));
    private final Label playerWhiteRank = new Label("");

    private final Label moveLabel = new Label("");

    public EditorActionToolBar() {
        toolButtonGroup = new ToggleGroup();
        toolMenuGroup = new ToggleGroup();

        editToolActions = Arrays.stream(EditorTool.values())
                                .map(value -> {
                                    var action = value.createAction(toolButtonGroup, toolMenuGroup);
                                    action.setUserObject(value);
                                    action.setComponentCompact(true);
                                    return action;
                                })
                                .sorted(Comparator.comparingDouble(EditorAction::getMenuPosition))
                                .collect(Collectors.toList());

        for (EditorToggleAction action : editToolActions) {
            YiToggleButton button = action.getAsComponent();
            assert button != null : "Editor tool button shouldn't be null";
            EditorTool editorTool = (EditorTool) action.getUserObject().orElseThrow();
            button.getStyleClass().add("button-style2");
            getItems().add(button);

            if (editorTool == EditorTool.PLAY_MOVE) {
                this.toolPlayMove = button;
            }
        }

        assert this.toolPlayMove != null : "No \"Play Move\" tool was found when constructing toolbar";

//        toolButtonGroup.getToggles().get(0).setSelected(true);

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

        getStyleClass().add("bg-black-60");
    }

    private EditorPerspective currentLayout = null;
    public void setContentForLayout(EditorPerspective layout, @Nullable GameModel gameModel) {
        getItems().clear();

        if (layout == EditorPerspective.REVIEW) {
            getItems().add(dynamicSpacer());
            addReviewTools();
            getItems().add(dynamicSpacer());
        } else if (layout == EditorPerspective.COMPACT) {
            toolPlayMove.setSelected(true); // This view is mainly for browsing
            addCompactGameInfo(gameModel);
        }

        this.currentLayout = layout;
    }

    private void addCompactGameInfo(@Nullable GameModel gameModel) {
        if (gameModel != null) {
            updateGameModelInfo(gameModel);
        }

        getItems().add(staticSpacer(4));
        getItems().add(playerBlackName);
        if (!playerBlackRank.getText().isBlank()) {
            getItems().add(playerBlackRank);
        }
        getItems().add(staticSpacer(8));

        getItems().add(playerWhiteName);
        if (!playerWhiteRank.getText().isBlank()) {
            getItems().add(playerWhiteRank);
        }

        getItems().add(dynamicSpacer());

        getItems().add(moveLabel);
        getItems().add(staticSpacer(4));
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

    private void addReviewTools() {
        for (EditorAction action : editToolActions) {
            getItems().add(action.getAsComponent());
        }
    }

    private Pane dynamicSpacer() {
        var dynamicSpacer = new Pane();
        HBox.setHgrow(dynamicSpacer, Priority.SOMETIMES);

        return dynamicSpacer;
    }

    private Pane staticSpacer(int width) {
        var spacer = new Pane();
        spacer.setPrefWidth(width);

        return spacer;
    }

    public void setGameModel(@NotNull GameModel newModel) {
        newModel.onCurrentNodeChange().addListener(newValue -> updateMoveInfo(newValue.getNode().getMoveNumber()));

        updateGameModelInfo(newModel);

        if (currentLayout == EditorPerspective.COMPACT) {
            setContentForLayout(EditorPerspective.COMPACT, newModel);
        }
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

    public void addSelectedToolChangeListener(PropertyListener<EditorTool> listener) {
        this.selectedTool.addListener(listener);
    }

    public List<EditorToggleAction> getAllActions() {
        return editToolActions;
    }
}
