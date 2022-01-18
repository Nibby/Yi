package codes.nibby.yi.app.components;

import codes.nibby.yi.app.framework.AppWindow;
import codes.nibby.yi.app.dialogs.GameModelEditDialog;
import codes.nibby.yi.app.framework.AppAccelerator;
import codes.nibby.yi.app.framework.AppText;
import codes.nibby.yi.app.framework.YiStyleClass;
import codes.nibby.yi.app.framework.action.AppAction;
import codes.nibby.yi.app.framework.action.AppActionManager;
import codes.nibby.yi.app.framework.action.AppBasicAction;
import codes.nibby.yi.app.framework.action.AppSeparatorAction;
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
import codes.nibby.yi.app.i18n.TextResource;
import codes.nibby.yi.app.utilities.IconUtilities;
import codes.nibby.yi.models.GameModel;
import codes.nibby.yi.models.GameModelInfo;

import java.util.*;
import java.util.function.Consumer;

import static codes.nibby.yi.app.framework.AppText.MOVE_COUNT;

public final class AppFooterToolBar extends BorderPane implements AppComponent<BorderPane> {

    // TODO: Stop hard coding these paths everywhere
    private final Label playerBlackName = new Label("", IconUtilities.loadIcon("/codes/nibby/yi/app/icons/blackStone_white32.png", getClass(), 16).orElse(null));
    private final Label playerBlackRank = new Label("");
    private final Label playerWhiteName = new Label("", IconUtilities.loadIcon("/codes/nibby/yi/app/icons/whiteStone_white32.png", getClass(), 16).orElse(null));
    private final Label playerWhiteRank = new Label("");

    private final List<AppAction> navActions = new ArrayList<>();
    private final AppAction toPrevious1Action = createNavAction(
        AppText.TO_PREVIOUS_NODE,
        GameModel::toPreviousNode,
        "/codes/nibby/yi/app/icons/arrowUp_white32.png",
        AppAccelerator.TO_PREVIOUS_NODE,
        0d
    );

    private final AppAction toPrevious10Action = createNavAction(
        AppText.TO_PREVIOUS_10_NODES,
        model -> model.toPreviousNode(10),
        "/codes/nibby/yi/app/icons/arrowUpDouble_white32.png",
        AppAccelerator.TO_PREVIOUS_10_NODES,
        0.01d
    );

    private final AppAction toRootAction = createNavAction(
        AppText.TO_ROOT_NODE,
        model -> model.setCurrentNode(model.getRootNode()),
        "/codes/nibby/yi/app/icons/arrowUpmost_white32.png",
        AppAccelerator.TO_ROOT_NODE,
        0.02d
    );

    private final AppAction toNext1Action = createNavAction(
        AppText.TO_NEXT_NODE,
        GameModel::toNextNode,
        "/codes/nibby/yi/app/icons/arrowDown_white32.png",
        AppAccelerator.TO_NEXT_NODE,
        0.03d
    );

    private final AppAction toNext10Action = createNavAction(
        AppText.TO_NEXT_10_NODES,
        model -> model.toNextNode(10),
        "/codes/nibby/yi/app/icons/arrowDownDouble_white32.png",
        AppAccelerator.TO_NEXT_10_NODES,
        0.04d
    );

    private final AppAction toVariationEndAction = createNavAction(
        AppText.TO_VARIATION_END,
        model -> model.toNextNode(Integer.MAX_VALUE),
        "/codes/nibby/yi/app/icons/arrowDownmost_white32.png",
        AppAccelerator.TO_VARIATION_END,
        0.05d
    );

    {
        var navMenuSeparator = new AppSeparatorAction();
        navMenuSeparator.setInMenuBar(AppMainMenuType.NAVIGATE, 0.025d);
        navActions.add(navMenuSeparator);
    }

    private final Label moveLabel = new Label("");
    private final AppAction editModelInfoAction = new AppBasicAction(AppText.EDIT_GAME_INFO);
    {
        IconUtilities.loadIcon("/codes/nibby/yi/app/icons/editMode_white32.png", AppFooterToolBar.class)
            .ifPresent(icon -> {
                var recoloredIcon = IconUtilities.flatColorSwap(icon.getImage(), 180, 180, 180);
                editModelInfoAction.setIcon(new ImageView(recoloredIcon));
            });
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

    private AppAction createNavAction(
        TextResource text,
        Consumer<GameModel> action,
        String iconPath,
        AppAccelerator accelerator,
        double menuPosition
    ) {
        var actionItem = new AppBasicAction(text, context -> {
            AppWindow window = context.getInvokerWindow();
            GameModel model = window.getGameModel();
            action.accept(model);
        });
        actionItem.setInMenuBar(AppMainMenuType.NAVIGATE, menuPosition);
        IconUtilities.loadIcon(iconPath, AppFooterToolBar.class, 16).ifPresent(originalIconView -> {
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
