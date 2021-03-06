package yi.editor.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;
import yi.component.shared.component.YiStyleClass;
import yi.component.shared.i18n.TextResource;
import yi.component.shared.utilities.IconUtilities;
import yi.core.go.GameModel;
import yi.core.go.GameModelInfo;
import yi.editor.EditorWindow;
import yi.editor.dialogs.EditorGameModelEditDialog;
import yi.editor.framework.EditorAccelerator;
import yi.editor.framework.EditorTextResources;
import yi.editor.framework.action.EditorAction;
import yi.editor.framework.action.EditorActionManager;
import yi.editor.framework.action.EditorBasicAction;
import yi.editor.framework.action.EditorSeparatorAction;

import java.util.*;
import java.util.function.Consumer;

import static yi.editor.framework.EditorTextResources.MOVE_COUNT;

public class EditorFooterToolBar extends BorderPane implements EditorComponent<BorderPane> {

    private final Label playerBlackName = new Label("", IconUtilities.loadIcon("/yi/editor/icons/blackStone_white32.png", getClass(), 16).orElse(null));
    private final Label playerBlackRank = new Label("");
    private final Label playerWhiteName = new Label("", IconUtilities.loadIcon("/yi/editor/icons/whiteStone_white32.png", getClass(), 16).orElse(null));
    private final Label playerWhiteRank = new Label("");

    private final List<EditorAction> navActions = new ArrayList<>();
    private final EditorAction toPrevious1Action = createNavAction(
            EditorTextResources.TO_PREVIOUS_NODE,
            GameModel::toPreviousNode,
            "/yi/editor/icons/arrowUp_white32.png",
            EditorAccelerator.TO_PREVIOUS_NODE,
            0d
    );

    private final EditorAction toPrevious10Action = createNavAction(
            EditorTextResources.TO_PREVIOUS_10_NODES,
            model -> model.toPreviousNode(10),
            "/yi/editor/icons/arrowUpDouble_white32.png",
            EditorAccelerator.TO_PREVIOUS_10_NODES,
            0.01d
    );

    private final EditorAction toRootAction = createNavAction(
            EditorTextResources.TO_ROOT_NODE,
            model -> model.setCurrentNode(model.getRootNode()),
            "/yi/editor/icons/arrowUpmost_white32.png",
            EditorAccelerator.TO_ROOT_NODE,
            0.02d
    );

    private final EditorAction toNext1Action = createNavAction(
            EditorTextResources.TO_NEXT_NODE,
            GameModel::toNextNode,
            "/yi/editor/icons/arrowDown_white32.png",
            EditorAccelerator.TO_NEXT_NODE,
            0.03d
    );

    private final EditorAction toNext10Action = createNavAction(
            EditorTextResources.TO_NEXT_10_NODES,
            model -> model.toNextNode(10),
            "/yi/editor/icons/arrowDownDouble_white32.png",
            EditorAccelerator.TO_NEXT_10_NODES,
            0.04d
    );

    private final EditorAction toVariationEndAction = createNavAction(
            EditorTextResources.TO_VARIATION_END,
            model -> model.toNextNode(Integer.MAX_VALUE),
            "/yi/editor/icons/arrowDownmost_white32.png",
            EditorAccelerator.TO_VARIATION_END,
            0.05d
    );

    {
        var navMenuSeparator = new EditorSeparatorAction();
        navMenuSeparator.setInMenuBar(EditorMainMenuType.NAVIGATE, 0.025d);
        navActions.add(navMenuSeparator);
    }

    private final Label moveLabel = new Label("");
    private final EditorAction editModelInfoAction = new EditorBasicAction(EditorTextResources.EDIT_GAME_INFO);
    {
        IconUtilities.loadIcon("/yi/editor/icons/editMode_white32.png", EditorFooterToolBar.class)
            .ifPresent(icon -> {
                var recoloredIcon = IconUtilities.flatColorSwap(icon.getImage(), 180, 180, 180);
                editModelInfoAction.setIcon(new ImageView(recoloredIcon));
            });
        editModelInfoAction.setAccelerator(EditorAccelerator.EDIT_GAME_INFO);
        editModelInfoAction.setInMenuBar(EditorMainMenuType.FILE, 0.8f);
        editModelInfoAction.setShowIconOnMenuItem(false);
        editModelInfoAction.setComponentCompact(true);
        Node node = editModelInfoAction.getAsComponent();
        if (node != null) {
            node.getStyleClass().add("button-style3");
        }
    }

    public EditorFooterToolBar() {
        // TODO: Consider putting these CSS class strings into a constant class ...
        decorateAsNameLabel(playerBlackName);
        decorateAsNameLabel(playerWhiteName);
        decorateAsRankLabel(playerBlackRank);
        decorateAsRankLabel(playerWhiteRank);

        moveLabel.getStyleClass().add(YiStyleClass.FOREGROUND_DARK.getName());
        getStyleClass().add(YiStyleClass.BACKGROUND_DARK.getName());

        setPrefHeight(35);

        var mainToolBar = new HBox();
        mainToolBar.setAlignment(Pos.CENTER_LEFT);
        mainToolBar.getChildren().setAll(
                createHBoxSpacer(16),
                playerBlackName,
                playerBlackRank,
                createHBoxSpacer(16),
                playerWhiteName,
                playerWhiteRank
        );
        setCenter(mainToolBar);

        var rightToolBar = new HBox();
        rightToolBar.setAlignment(Pos.CENTER_RIGHT);
        rightToolBar.getChildren().setAll(
                toRootAction.getAsComponent(),
                toPrevious10Action.getAsComponent(),
                toPrevious1Action.getAsComponent(),
                moveLabel,
                toNext1Action.getAsComponent(),
                toNext10Action.getAsComponent(),
                toVariationEndAction.getAsComponent(),
                createHBoxSpacer(8)
        );
        setRight(rightToolBar);
    }

    private void decorateAsRankLabel(Label label) {
        label.getStyleClass().addAll(
            YiStyleClass.FOREGROUND_DARK_SECONDARY.getName(),
            YiStyleClass.FONT_WEIGHT_NORMAL.getName(),
            YiStyleClass.FONT_SIZE_14.getName()
        );
        label.setMaxWidth(50);
    }

    private void decorateAsNameLabel(Label label) {
        label.getStyleClass().addAll(
            YiStyleClass.FOREGROUND_LIGHT.getName(),
            YiStyleClass.FONT_WEIGHT_BOLD.getName(),
            YiStyleClass.FONT_SIZE_16.getName(),
            YiStyleClass.PREFERRED_HEIGHT_28.getName()
        );
        label.setMaxWidth(160);
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
        editModelInfoAction.setAction(context -> {
            var modelEditDialog = new EditorGameModelEditDialog(newModel);
            modelEditDialog.setCloseCallback(button -> {
                if (button == modelEditDialog.actionButton) {
                    modelEditDialog.applyChangesToGameModel();
                }
                return true;
            });
            context.getEditorWindow().pushModalContent(modelEditDialog);
        });
    }

    public void onGameInfoUpdate(final String key, Object newValue) {
        switch (key) {
            case GameModelInfo.KEY_PLAYER_BLACK_NAME:
                playerBlackName.setText(newValue.toString());
                break;
            case GameModelInfo.KEY_PLAYER_BLACK_RANK:
                setRankText(playerBlackRank, newValue.toString());
                break;
            case GameModelInfo.KEY_PLAYER_WHITE_NAME:
                playerWhiteName.setText(newValue.toString());
                break;
            case GameModelInfo.KEY_PLAYER_WHITE_RANK:
                setRankText(playerWhiteRank, newValue.toString());
                break;
        }
    }

    private void setRankText(Label rankLabel, String text) {
        rankLabel.setText(text);
        boolean noText = text.isBlank();
        rankLabel.setVisible(!noText);
        rankLabel.setManaged(!noText);
    }

    private Node createHBoxSpacer(int width) {
        var spacer = new Pane();
        HBox.setMargin(spacer, new Insets(0, width/2f, 0, width/2f));
        return spacer;
    }

    private EditorAction createNavAction(TextResource text,
                                         Consumer<GameModel> action,
                                         String iconPath,
                                         EditorAccelerator accelerator,
                                         double menuPosition) {
        var actionItem = new EditorBasicAction(text, context -> {
            EditorWindow window = context.getEditorWindow();
            GameModel model = window.getGameModel();
            action.accept(model);
        });
        actionItem.setInMenuBar(EditorMainMenuType.NAVIGATE, menuPosition);
        IconUtilities.loadIcon(iconPath, EditorFooterToolBar.class, 16).ifPresent(originalIconView -> {
            Image originalIcon = originalIconView.getImage();
            Image icon = IconUtilities.flatColorSwap(originalIcon, 180, 180, 180);
            actionItem.setIcon(new ImageView(icon));
        });
        actionItem.setAccelerator(accelerator);
        actionItem.setShowIconOnMenuItem(false);
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
        Set<EditorAction> allActions = new HashSet<>(navActions);
        allActions.add(editModelInfoAction);
        return allActions.toArray(new EditorAction[0]);
    }

    @Override
    public Optional<BorderPane> getComponent() {
        return Optional.of(this);
    }
}
