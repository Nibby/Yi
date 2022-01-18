package codes.nibby.yi.app.framework;

import codes.nibby.yi.app.components.*;
import codes.nibby.yi.app.framework.action.AppAction;
import codes.nibby.yi.app.framework.action.AppActionManager;
import codes.nibby.yi.app.hack.Hacks;
import codes.nibby.yi.app.settings.AppSettings;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import org.jetbrains.annotations.NotNull;
import codes.nibby.yi.app.components.board.GameBoardViewer;
import codes.nibby.yi.app.components.comment.GameCommentViewer;
import codes.nibby.yi.app.framework.property.Property;
import codes.nibby.yi.app.framework.property.PropertyListener;
import codes.nibby.yi.app.utilities.GuiUtilities;
import codes.nibby.yi.app.utilities.SystemUtilities;
import codes.nibby.yi.app.components.tree.GameTreeViewer;
import codes.nibby.yi.app.components.tree.GameTreeViewerSettings;
import codes.nibby.yi.models.GameModel;
import codes.nibby.yi.models.GameModelImporter;
import codes.nibby.yi.models.GameParseException;
import codes.nibby.yi.models.StandardGameRules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The main window for interacting with one {@link GameModel}.
 */
public class AppWindow extends YiWindow {

    private static final GameModel DEFAULT_MODEL = new GameModel(1, 1, StandardGameRules.CHINESE);
    private static final List<AppWindow> ACTIVE_WINDOWS = new ArrayList<>();

    private final Property<AppPerspective> perspective = new Property<>(AppPerspective.NONE);
    private final AppActionManager actionManager;
    private final AppUndoSupport undoSystem;

    private final AppMenuBar menuBar;
    private final AppBoardArea boardArea;
    private final GameTreeViewer treeViewer;
    private final GameCommentViewer commentViewer;
    private final AppFooterToolBar footerToolBar;

    private final Property<GameModel> gameModel = new Property<>(DEFAULT_MODEL);

    private boolean addedMenuBarOnce = false;

    public AppWindow(GameModel gameModel) {
        this(gameModel, AppSettings.general.getPerspective());
    }

    public AppWindow(GameModel gameModel, AppPerspective perspective) {
        super();

        actionManager = new AppActionManager(this);
        undoSystem = new AppUndoSupport();
        addComponent(undoSystem);

        footerToolBar = new AppFooterToolBar();
        addComponent(footerToolBar);

        boardArea = new AppBoardArea();
        addComponent(boardArea);

        enableDragAndDropToOpenFile(boardArea.getGameBoardViewer());

        var treeViewerSettings = new GameTreeViewerSettings();
        // TODO: Extract these out to settings.json
        treeViewerSettings.setBackgroundColor(GuiUtilities.getColor(43, 43, 43));
        treeViewerSettings.setNodeColor(GuiUtilities.getColor(90, 90, 90));
        treeViewerSettings.setNodeHoverColor(GuiUtilities.getColor(170, 170, 170));
        treeViewerSettings.setNodeInCurrentVariationColor(GuiUtilities.getColor(203, 203, 203));
        treeViewerSettings.setNodeWithCommentaryColor(GuiUtilities.getColor(87, 125, 186));
        treeViewerSettings.setCurrentNodeColor(GuiUtilities.getColor(255, 255, 255));
        treeViewerSettings.setPreviewPromptForeground(GuiUtilities.getColor(200, 200, 200));
        treeViewerSettings.setPreviewPromptBackground(GuiUtilities.getColor(30, 30, 30));
        treeViewerSettings.setPreviewTextResource(AppText.PREVIEW_MOVE_PROMPT);
        treeViewerSettings.setPreviewTextFont(YiFontManager.getDefaultFont(13));
        treeViewerSettings.setPreviewPromptEnabled(true);

        commentViewer = new GameCommentViewer();

        treeViewer = new GameTreeViewer();
        treeViewer.setSettings(treeViewerSettings);

        addGameModelChangeListener(newModel -> {
            boardArea.setGameModel(newModel);
            treeViewer.setGameModel(newModel);
            undoSystem.setGameModel(newModel);
            commentViewer.setGameModel(newModel);
            footerToolBar.setGameModel(newModel);
        });

        treeViewer.addPreviewNodeChangeListener(boardArea::onHighlightedNodeChange);
        boardArea.getGameBoardViewer().addPreviewNodeChangeListener(newPreview -> {
            if (newPreview != treeViewer.getHighlightedNode()) {
                treeViewer.setHighlightedNodePathAndCenterCamera(newPreview);
            }

            var nodeToShowCommentsFor = newPreview == null
                                            ? getGameModel().getCurrentNode()
                                            : newPreview;

            commentViewer.setEditable(nodeToShowCommentsFor == getGameModel().getCurrentNode());
            commentViewer.setCommentText(nodeToShowCommentsFor);
        });

        initializeActions(actionManager);
        Hacks.fixSingleKeyAcceleratorsForMac(actionManager.getAllActions(), getScene());

        // Current action system implementation requires all actions to be created prior
        // to creating the menu bar. After this point newly created actions will not be
        // added to the menu bar.
        //
        // TODO: Consider supporting late-comers to remove this temporal coupling...
        menuBar = new AppMenuBar(actionManager.getContext(), actionManager.getAllActions());

        setPerspective(perspective);
        setGameModel(gameModel);
        getStage().setTitle(GlobalHelper.getProgramName());
        setIcons();

        ACTIVE_WINDOWS.add(this);
        getStage().onCloseRequestProperty().addListener(event -> ACTIVE_WINDOWS.remove(this));
    }

    private void setIcons() {
        final String baseDir = "/codes/nibby/yi/app/icons/stage/";
        if (!SystemUtilities.isMac()) {
            getStage().getIcons().addAll(
                new Image(AppWindow.class.getResourceAsStream(baseDir + "icon16.png")),
                new Image(AppWindow.class.getResourceAsStream(baseDir + "icon32.png")),
                new Image(AppWindow.class.getResourceAsStream(baseDir + "icon64.png")),
                new Image(AppWindow.class.getResourceAsStream(baseDir + "icon128.png"))
            );
        }
    }

    /**
     * @implNote This method is a least-effort solution to the problem that
     * actions initialised after the creation of {@link AppMenuBar} will not
     * have its menu component added to the main menu. This is a common issue
     * in tests where test code needs to instantiate custom actions that also
     * require {@link AppActionManager} to be instantiated, but before
     * main menu bar is created.
     * <p/>
     * For now, we solve this problem by providing this inheritable method so that
     * additional actions can be loaded to this window.
     *
     * @param actionManager Action manager for this editor window.
     */
    protected void initializeActions(AppActionManager actionManager) {
        var actionHandler = new AppStandardActionsHandler();
        var standardActions = new AppStandardActions(actionHandler);
        addComponent(standardActions);
        actionManager.addActions(AppPerspective.createActions());
    }

    private void enableDragAndDropToOpenFile(GameBoardViewer boardViewer) {
        boardViewer.setDragAndDropBehaviour(files -> {
            var success = false;
            if (files.size() == 1) {
                var file = files.get(0);
                try {
                    var importedGameModel = GameModelImporter.INSTANCE.fromFile(file.toPath());
                    setGameModel(importedGameModel);
                    success = true;
                } catch (GameParseException | IOException e) {
                    // TODO: Handle this in an error dialog so that users know something
                    //       went wrong with their file rather than throwing a stack trace
                    //       in their faces.
                    e.printStackTrace();
                }
            }
            return success;
        });
    }

    public void setGameModel(@NotNull GameModel newModel) {
        if (this.gameModel.get() != null) {
            this.gameModel.get().dispose();
        }
        this.gameModel.set(newModel);
        this.actionManager.refreshActions();
    }

    public @NotNull GameModel getGameModel() {
        Objects.requireNonNull(gameModel.get(), "No game model is set. " +
                "setGameModel() must be called once before calling getGameModel().");

        return gameModel.get();
    }

    public void setPerspective(@NotNull AppPerspective newLayout) {
        if (this.perspective.get() == newLayout) {
            return; // Avoid flickering when setting the same layout
        }
        boardArea.setContentForPerspective(newLayout);

        var content = newLayout.getContent(this);
        var controlPane = new BorderPane();

        // Work around bug on macOS where setting the menu bar again causes the
        // menu bar to disappear. Possibly related to native code for system-style
        // menu bars...
        var addMenuBar = SystemUtilities.isMac() && !addedMenuBarOnce || !menuBar.isUseSystemMenuBar();
        if (addMenuBar) {
            setMainMenuBar(menuBar);
        }
        if (!addedMenuBarOnce) {
            addedMenuBarOnce = true;
        }

        var container = new BorderPane();
        container.setTop(controlPane);
        container.setCenter(content);

        var scene = getScene();
        var currentContent = scene.getContent();

        if (scene.isContentSet()) {
            double currentHeight = currentContent.getBoundsInLocal().getHeight();
            double newAspectRatio = newLayout.getPreferredAspectRatio();
            double newWidth = currentHeight * newAspectRatio;

            scene.setContent(container);
            setWidth(newWidth);
        } else {
            var startupSize = newLayout.getMinimumWindowSize();

            double startupWidth = startupSize.getWidth();
            double startupHeight = startupSize.getHeight();

            scene.setContent(container);
            setWidth(startupWidth);
            setHeight(startupHeight);
        }

        var minSize = newLayout.getMinimumWindowSize();
        setMinWidth(minSize.getWidth());
        setMinHeight(minSize.getHeight());

        if (getWidth() < minSize.getWidth()) {
            setWidth(minSize.getWidth());
        }
        if (getHeight() < minSize.getHeight()) {
            setHeight(minSize.getHeight());
        }

        this.perspective.set(newLayout);
        AppSettings.general.setPerspective(newLayout);
    }

    private <ComponentType> void addComponent(AppComponent<ComponentType> component) {
        for (AppAction action : component.getActions(actionManager)) {
            if (action.isInMenuBar() && menuBar != null) {
                // Current action system implementation requires all actions to be created
                // prior to creating the menu bar. In other words, if an action is to
                // export a menu item, it should be done in this class constructor, or
                // by overriding initializeActions() and creating it there.
                throw new IllegalStateException("Main menu is already created. " +
                        "The action '" + action.getLocalisedText() + "' will not be " +
                        "included in the main menu despite being requested to do so.");
            }
            actionManager.addAction(action);
        }
    }

    public Parent getBoardComponent() {
        return boardArea.getComponent()
                        .orElseThrow(() -> new IllegalStateException("Board component not initialized."));
    }

    public Parent getTreeComponent() {
        return treeViewer.getComponent();
    }

    public Parent getCommentComponent() {
        return commentViewer.getComponent();
    }

    public AppBoardArea getBoardArea() {
        return boardArea;
    }

    public void addGameModelChangeListener(PropertyListener<GameModel> listener) {
        gameModel.addListener(listener);
    }

    public AppMenuBar getMainMenuBar() {
        return menuBar;
    }

    public AppPerspective getPerspective() {
        return perspective.get();
    }

    public static int getActiveWindowCount() {
        return ACTIVE_WINDOWS.size();
    }

    public Node getFooterToolBar() {
        return footerToolBar;
    }
}
