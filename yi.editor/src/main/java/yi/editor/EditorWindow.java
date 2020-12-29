package yi.editor;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import yi.component.boardviewer.GameBoardViewer;
import yi.component.commentviewer.GameCommentViewer;
import yi.component.shared.Property;
import yi.component.shared.PropertyListener;
import yi.component.shared.component.FontManager;
import yi.component.shared.component.YiScene;
import yi.component.shared.utilities.GuiUtilities;
import yi.component.shared.utilities.SystemUtilities;
import yi.component.treeviewer.GameTreeViewer;
import yi.component.treeviewer.GameTreeViewerSettings;
import yi.core.go.GameModel;
import yi.core.go.GameModelImporter;
import yi.core.go.GameParseException;
import yi.core.go.StandardGameRules;
import yi.editor.components.*;
import yi.editor.framework.EditorComponent;
import yi.editor.framework.EditorHelper;
import yi.editor.framework.action.EditorAction;
import yi.editor.framework.action.EditorActionManager;
import yi.editor.settings.EditorSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The main window for the {@link GameModel} editor tool.
 */
public class EditorWindow extends Stage {

    private static final GameModel DEFAULT_MODEL = new GameModel(1, 1, StandardGameRules.CHINESE);
    private static final List<EditorWindow> ACTIVE_WINDOWS = new ArrayList<>();

    private final Property<EditorPerspective> perspective = new Property<>(EditorPerspective.NONE);
    private final EditorActionManager actionManager;
    private final EditorMenuBar menuBar;
    private final EditorToolBar toolBar;
    private final EditorUndoSystem undoSystem;

    private final EditorBoardArea boardArea;
    private final GameTreeViewer treeViewer;
    private final GameCommentViewer commentViewer;
    private final Property<GameModel> gameModel = new Property<>(DEFAULT_MODEL);

    private boolean addedMenuBarOnce = false;

    public EditorWindow(GameModel gameModel) {
        this(gameModel, EditorSettings.general.getPerspective());
    }

    public EditorWindow(GameModel gameModel, EditorPerspective perspective) {
        actionManager = new EditorActionManager(this);
        undoSystem = new EditorUndoSystem();
        addComponent(undoSystem);

        toolBar = new EditorToolBar();
        boardArea = new EditorBoardArea();
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
        treeViewerSettings.setPreviewTextResource(EditorTextResources.PREVIEW_MOVE_PROMPT);
        treeViewerSettings.setPreviewTextFont(FontManager.getDefaultFont(13));
        treeViewerSettings.setPreviewPromptEnabled(true);

        commentViewer = new GameCommentViewer();

        treeViewer = new GameTreeViewer();
        treeViewer.setSettings(treeViewerSettings);

        addGameModelChangeListener(newModel -> {
            boardArea.setGameModel(newModel);
            treeViewer.setGameModel(newModel);
            undoSystem.setGameModel(newModel, boardArea);
            commentViewer.setGameModel(newModel);
            toolBar.setGameModel(newModel);
        });

        treeViewer.addPreviewNodeChangeListener(boardArea::onHighlightedNodeChange);
        boardArea.getGameBoardViewer().addPreviewNodeChangeListener(newPreview -> {
            if (newPreview != treeViewer.getHighlightedNode()) {
                treeViewer.setHighlightedNodePath(newPreview);
            }

            var nodeToShowCommentsFor = newPreview == null
                                            ? getGameModel().getCurrentNode()
                                            : newPreview;
            commentViewer.setText(nodeToShowCommentsFor.getComments());
        });

        initializeActions(actionManager);

        // Current action system implementation requires all actions to be created prior
        // to creating the menu bar. After this point newly created actions will not be
        // added to the menu bar.
        //
        // TODO: Consider supporting late-comers to remove this temporal coupling...
        menuBar = new EditorMenuBar(actionManager.getContext(), actionManager.getAllActions());

        setPerspective(perspective);
        setGameModel(gameModel);
        setTitle(EditorHelper.getProgramName());

        ACTIVE_WINDOWS.add(this);
        onCloseRequestProperty().addListener(event -> ACTIVE_WINDOWS.remove(this));
    }

    /**
     * @implNote This method is a least-effort solution to the problem that
     * actions initialised after the creation of {@link EditorMenuBar} will not
     * have its menu component added to the main menu. This is a common issue
     * in tests where test code needs to instantiate custom actions that also
     * require {@link EditorActionManager} to be instantiated, but before
     * main menu bar is created.
     * <p/>
     * For now, we solve this problem by providing this inheritable method so that
     * additional actions can be loaded to this window.
     *
     * @param actionManager Action manager for this editor window.
     */
    protected void initializeActions(EditorActionManager actionManager) {
        var actionHandler = new EditorStandardActionsHandler();
        var standardActions = new EditorStandardActions(actionHandler);
        addComponent(standardActions);
        actionManager.addActions(EditorPerspective.createActions());
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

    public void setPerspective(@NotNull EditorPerspective newLayout) {
        if (this.perspective.get() == newLayout) {
            return; // Avoid flickering when setting the same layout
        }
        boardArea.setContentForLayout(newLayout);
        toolBar.setContentForLayout(newLayout);

        var content = newLayout.getContent(this);

        var controlPane = new BorderPane();

        // Work around bug on macOS where setting the menu bar again causes the
        // menu bar to disappear. Possibly related to native code for system-style
        // menu bars...
        var addMenuBar = SystemUtilities.isMac() && !addedMenuBarOnce || !menuBar.isUseSystemMenuBar();
        if (addMenuBar) {
            controlPane.setTop(menuBar);
        }
        if (!addedMenuBarOnce) {
            addedMenuBarOnce = true;
        }
        controlPane.setCenter(toolBar);

        var container = new BorderPane();
        container.setTop(controlPane);
        container.setCenter(content);

        var currentScene = getScene();

        if (currentScene != null) {
            double currentHeight = currentScene.getHeight();
            double newAspectRatio = newLayout.getPreferredAspectRatio();
            double newWidth = currentHeight * newAspectRatio;

            var newScene = new YiScene(container, newWidth, currentHeight);
            setYiScene(newScene);
            setWidth(newWidth);
        } else {
            var startupSize = newLayout.getMinimumWindowSize();

            double startupWidth = startupSize.getWidth();
            double startupHeight = startupSize.getHeight();

            var newScene = new YiScene(container, startupWidth, startupHeight);
            setYiScene(newScene);
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
        EditorSettings.general.setPerspective(newLayout);
    }

    private <ComponentType> void addComponent(EditorComponent<ComponentType> component) {
        for (EditorAction action : component.getActions(actionManager)) {
            if (action.isInMainMenu() && menuBar != null) {
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

    /**
     * Wrapper for {@link #setScene(Scene)} but also runs some custom routines.
     * It is highly recommended to use this method rather than the base {@link #setScene(Scene)}
     * so that scenes support as many editor features as possible.
     *
     * @param newScene Scene to set.
     */
    private void setYiScene(@NotNull YiScene newScene) {
        setScene(newScene);
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

    public EditorBoardArea getBoardArea() {
        return boardArea;
    }

    public void addGameModelChangeListener(PropertyListener<GameModel> listener) {
        gameModel.addListener(listener);
    }

    public EditorMenuBar getMainMenuBar() {
        return menuBar;
    }

    public EditorPerspective getPerspective() {
        return perspective.get();
    }

    public static int getActiveWindowCount() {
        return ACTIVE_WINDOWS.size();
    }
}
