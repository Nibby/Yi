package yi.editor.components;

import javafx.scene.control.ButtonBase;
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
import yi.component.YiToggleButton;
import yi.common.i18n.TextResource;
import yi.core.go.GameModel;
import yi.core.go.GameModelInfo;
import yi.editor.EditorTool;
import yi.editor.TextKeys;
import yi.editor.utilities.IconUtilities;

import java.util.concurrent.atomic.AtomicBoolean;

import static yi.editor.TextKeys.*;

/**
 * Primary toolbar for {@link yi.editor.EditorFrame} that displays a set of supported editing tools
 * and other options.
 */
public class EditorActionToolBar extends ToolBar {

    private final Property<EditorTool> selectedTool = new Property<>(EditorTool.PLAY_MOVE);

    private final ToggleGroup toolButtonGroup;

    private final YiToggleButton toolPlayMove;
    private final YiToggleButton toolAddBlackStone;
    private final YiToggleButton toolAddWhiteStone;
    private final YiToggleButton toolAnnotateTriangle;
    private final YiToggleButton toolAnnotateCircle;
    private final YiToggleButton toolAnnotateSquare;
    private final YiToggleButton toolAnnotateCross;
    private final YiToggleButton toolAnnotateText;
    private final YiToggleButton toolAnnotateNumber;
    private final YiToggleButton toolAnnotateLine;
    private final YiToggleButton toolAnnotateArrow;
    private final YiToggleButton toolAnnotateDim;

    private final Label playerBlackName = new Label("", IconUtilities.getIcon("/icons/blackStone32_white.png", getClass()).orElse(null));
    private final Label playerBlackRank = new Label("");
    private final Label playerWhiteName = new Label("", IconUtilities.getIcon("/icons/whiteStone32_white.png", getClass()).orElse(null));
    private final Label playerWhiteRank = new Label("");

    private final Label moveLabel = new Label("");

    public EditorActionToolBar() {
        toolButtonGroup = new ToggleGroup();

        toolPlayMove = createEditToolButton(EditorTool.PLAY_MOVE, "/icons/playStone32_white.png", TOOL_PLAY_MOVE);
        toolAddBlackStone = createEditToolButton(EditorTool.ADD_BLACK_STONE, "/icons/addBlackStone32_white.png", TOOL_ADD_BLACK);
        toolAddWhiteStone = createEditToolButton(EditorTool.ADD_WHITE_STONE, "/icons/addWhiteStone32_white.png", TOOL_ADD_WHITE);

        toolAnnotateTriangle = createEditToolButton(EditorTool.ANNOTATE_TRIANGLE, "/icons/annoTriangle32_white.png", TOOL_TRIANGLE);
        toolAnnotateCircle = createEditToolButton(EditorTool.ANNOTATE_CIRCLE, "/icons/annoCircle32_white.png", TOOL_CIRCLE);
        toolAnnotateSquare = createEditToolButton(EditorTool.ANNOTATE_SQUARE, "/icons/annoSquare32_white.png", TOOL_SQUARE);
        toolAnnotateCross = createEditToolButton(EditorTool.ANNOTATE_CROSS, "/icons/annoCross32_white.png", TOOL_CROSS);
        toolAnnotateText = createEditToolButton(EditorTool.ANNOTATE_LETTER, "/icons/annoLetter32_white.png", TOOL_LABEL_LETTER);
        toolAnnotateNumber = createEditToolButton(EditorTool.ANNOTATE_NUMBER, "/icons/annoNumber32_white.png", TOOL_LABEL_NUMBER);
        toolAnnotateLine = createEditToolButton(EditorTool.ANNOTATE_LINE, "/icons/annoLine32_white.png", TOOL_LINE);
        toolAnnotateArrow = createEditToolButton(EditorTool.ANNOTATE_ARROW, "/icons/annoArrow32_white.png", TOOL_ARROW);
        toolAnnotateDim = createEditToolButton(EditorTool.ANNOTATE_DIM, "/icons/annoDim32_white.png", TOOL_DIM);

        toolPlayMove.setSelected(true);

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

    private ContentLayout currentLayout = null;
    public void setContentForLayout(ContentLayout layout, @Nullable GameModel gameModel) {
        getItems().clear();

        if (layout == ContentLayout.REVIEW) {
            getItems().add(dynamicSpacer());
            addReviewTools();
            getItems().add(dynamicSpacer());
        } else if (layout == ContentLayout.COMPACT) {
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
                ? TextKeys.DEFAULT_BLACK_NAME.getLocalisedText() : blackName);

        var blackRank = gameModel.getInfo().getPlayerBlackRank();
        playerBlackRank.setVisible(!blackRank.isBlank());
        playerBlackRank.setManaged(!blackRank.isBlank());
        playerBlackRank.setText(gameModel.getInfo().getPlayerBlackRank());

        var whiteName = gameModel.getInfo().getPlayerWhiteName();
        playerWhiteName.setText(whiteName.isBlank()
                ? TextKeys.DEFAULT_WHITE_NAME.getLocalisedText() : whiteName);

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
        final int gap = 10;

        getItems().add(toolPlayMove);
        getItems().add(staticSpacer(gap));
        getItems().add(toolAddBlackStone);
        getItems().add(toolAddWhiteStone);
        getItems().add(staticSpacer(gap));
        getItems().add(toolAnnotateTriangle);
        getItems().add(toolAnnotateCircle);
        getItems().add(toolAnnotateSquare);
        getItems().add(toolAnnotateCross);
        getItems().add(toolAnnotateText);
        getItems().add(toolAnnotateNumber);
        getItems().add(toolAnnotateDim);
        getItems().add(toolAnnotateLine);
        getItems().add(toolAnnotateArrow);
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

        if (currentLayout == ContentLayout.COMPACT) {
            setContentForLayout(ContentLayout.COMPACT, newModel);
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

    private YiToggleButton createEditToolButton(EditorTool editorTool, String iconResource, TextResource tooltip) {
        var toggle = new YiToggleButton();
        toggle.setFocusTraversable(false);
        toggle.getStyleClass().add("button-style2");
        toggle.setTooltip(tooltip);
        setIcon(iconResource, toggle);

        // TODO: I am way too tired to work out why clicking on the already-selected
        //       toggle button causes the icon to go back to the unselected version,
        //       so I put this flag here to ignore every second update to the icon.
        //       I suspect toggle.setSelected(true) has something to do with it...
        //
        //       Nonetheless right now the code around this area is a very dirty trick.
        AtomicBoolean ignoreIconUpdate = new AtomicBoolean(false);
        toggle.selectedProperty().addListener((observable, wasSelected, isSelected) -> {
            if (isSelected) {
                selectedTool.set(editorTool);
            } else {
                if (toolButtonGroup.getSelectedToggle() == null && wasSelected) {
                    toggle.setSelected(true);
                    ignoreIconUpdate.set(true);
                }
            }

            if (!ignoreIconUpdate.get()) {
                if (isSelected) {
                    setIcon(iconResource.replace("_white", ""), toggle);
                } else {
                    setIcon(iconResource, toggle);
                }
            } else {
                ignoreIconUpdate.set(false);
            }
        });

        toolButtonGroup.getToggles().add(toggle);

        return toggle;
    }

    private void setIcon(String iconResource, ButtonBase buttonBase) {
        IconUtilities.getIcon(iconResource, getClass()).ifPresentOrElse(buttonBase::setGraphic, () -> buttonBase.setText("?"));
    }

    public void addSelectedToolChangeListener(PropertyListener<EditorTool> listener) {
        this.selectedTool.addListener(listener);
    }
}
