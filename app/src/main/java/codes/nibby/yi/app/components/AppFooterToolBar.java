package codes.nibby.yi.app.components;

import codes.nibby.yi.app.dialogs.GameModelEditDialog;
import codes.nibby.yi.app.framework.*;
import codes.nibby.yi.app.framework.action.AppAction;
import codes.nibby.yi.app.framework.action.AppActionManager;
import codes.nibby.yi.app.framework.action.AppBasicAction;
import codes.nibby.yi.app.framework.action.AppSeparatorAction;
import codes.nibby.yi.app.i18n.TextResource;
import codes.nibby.yi.models.GameModel;
import codes.nibby.yi.models.GameModelInfo;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

import static codes.nibby.yi.app.framework.AppText.MOVE_COUNT;

public final class AppFooterToolBar extends BorderPane implements AppComponent<BorderPane> {

    private final Label playerBlackName = new Label("", AppIcon.BLACK_STONE);
    private final Label playerBlackRank = new Label("");
    private final Label playerWhiteName = new Label("", AppIcon.WHITE_STONE);
    private final Label playerWhiteRank = new Label("");

    private final List<AppAction> navActions = new ArrayList<>();

    {
        createNavAction(
            AppText.TO_PREVIOUS_NODE,
            GameModel::toPreviousNode,
            AppIcon.ARROW_UP,
            AppAccelerator.TO_PREVIOUS_NODE,
            0d
        );

        createNavAction(
            AppText.TO_PREVIOUS_10_NODES,
            model -> model.toPreviousNode(10),
            AppIcon.ARROW_UP_DOUBLE,
            AppAccelerator.TO_PREVIOUS_10_NODES,
            0.01d
        );

        createNavAction(
            AppText.TO_ROOT_NODE,
            model -> model.setCurrentNode(model.getRootNode()),
            AppIcon.ARROW_UP_EDGE,
            AppAccelerator.TO_ROOT_NODE,
            0.02d
        );

        createNavAction(
            AppText.TO_NEXT_NODE,
            GameModel::toNextNode,
            AppIcon.ARROW_DOWN,
            AppAccelerator.TO_NEXT_NODE,
            0.03d
        );

        createNavAction(
            AppText.TO_NEXT_10_NODES,
            model -> model.toNextNode(10),
            AppIcon.ARROW_DOWN_DOUBLE,
            AppAccelerator.TO_NEXT_10_NODES,
            0.04d
        );

        createNavAction(
            AppText.TO_VARIATION_END,
            model -> model.toNextNode(Integer.MAX_VALUE),
            AppIcon.ARROW_DOWN_EDGE,
            AppAccelerator.TO_VARIATION_END,
            0.05d
        );

        var navMenuSeparator = new AppSeparatorAction();
        navMenuSeparator.setInMenuBar(AppMainMenuType.NAVIGATE, 0.025d);
        navActions.add(navMenuSeparator);
    }

    private final Label moveLabel = new Label("");
    private final AppAction editModelInfoAction = new AppBasicAction(AppText.EDIT_GAME_INFO);
    {
        editModelInfoAction.setIcon(AppIcon.PENCIL);
        editModelInfoAction.setAccelerator(AppAccelerator.EDIT_GAME_INFO);
        editModelInfoAction.setInMenuBar(AppMainMenuType.FILE, 0.81f);
        editModelInfoAction.setShowIconOnMenuItem(false);
        editModelInfoAction.setComponentCompact(true);
        Node node = editModelInfoAction.getAsComponent();
        if (node != null) {
            node.getStyleClass().add("button-style3");
        }
    }

    public AppFooterToolBar() {
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
            createHBoxSpacer(4),
            playerBlackRank,
            createHBoxSpacer(16),
            playerWhiteName,
            createHBoxSpacer(4),
            playerWhiteRank
        );
        setCenter(mainToolBar);

        var rightToolBar = new HBox();
        rightToolBar.setAlignment(Pos.CENTER_RIGHT);
        rightToolBar.getChildren().setAll(
            moveLabel,
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
        playerBlackName.setText(
            blackName.isBlank()
                ? AppText.DEFAULT_BLACK_NAME.getLocalisedText()
                : blackName
        );

        var blackRank = gameModel.getInfo().getPlayerBlackRank();
        playerBlackRank.setVisible(!blackRank.isBlank());
        playerBlackRank.setManaged(!blackRank.isBlank());
        playerBlackRank.setText(gameModel.getInfo().getPlayerBlackRank());

        var whiteName = gameModel.getInfo().getPlayerWhiteName();
        playerWhiteName.setText(
            whiteName.isBlank()
                ? AppText.DEFAULT_WHITE_NAME.getLocalisedText()
                : whiteName
        );

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
        newModel.onCurrentNodeChange().addListener(newValue ->
            updateMoveInfo(newValue.getNode().getMoveNumber())
        );

        updateGameModelInfo(newModel);
        editModelInfoAction.setAction(context -> {
            var modelEditDialog = new GameModelEditDialog(newModel);
            modelEditDialog.setCloseCallback(button -> {
                if (button == modelEditDialog.actionButton) {
                    modelEditDialog.applyChangesToGameModel();
                }
                return true;
            });
            context.getInvokerWindow().pushModalContent(modelEditDialog);
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

    private void createNavAction(
        TextResource text,
        Consumer<GameModel> action,
        AppIcon icon,
        AppAccelerator accelerator,
        double menuPosition
    ) {
        var actionItem = new AppBasicAction(text, context -> {
            AppWindow window = context.getInvokerWindow();
            GameModel model = window.getGameModel();
            action.accept(model);
        });
        actionItem.setInMenuBar(AppMainMenuType.NAVIGATE, menuPosition);
        actionItem.setIcon(icon);
        actionItem.setAccelerator(accelerator);
        actionItem.setShowIconOnMenuItem(false);
        actionItem.setComponentCompact(true);

        Node button = actionItem.getAsComponent();
        assert button != null;
        button.setFocusTraversable(false);
        button.getStyleClass().add("button-style3");

        navActions.add(actionItem);
    }

    @Override
    public @NotNull AppAction[] getActions(AppActionManager actionManager) {
        Set<AppAction> allActions = new HashSet<>(navActions);
        allActions.add(editModelInfoAction);
        return allActions.toArray(new AppAction[0]);
    }

    @Override
    public Optional<BorderPane> getComponent() {
        return Optional.of(this);
    }
}
