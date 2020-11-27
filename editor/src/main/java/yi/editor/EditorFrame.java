package yi.editor;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.YiScene;
import yi.component.board.GameBoardViewer;
import yi.component.gametree.GameTreeViewer;
import yi.component.gametree.GameTreeViewerSettings;
import yi.component.utilities.GuiUtilities;
import yi.component.utilities.SystemUtilities;
import yi.core.go.GameModel;
import yi.core.go.GameModelImporter;
import yi.core.go.GameParseException;
import yi.editor.components.*;
import yi.editor.settings.Settings;
import yi.editor.utilities.ValueListener;
import yi.editor.utilities.ValueListenerManager;

import java.io.IOException;
import java.util.Objects;

/**
 * The main frame for an editor session.
 */
public class EditorFrame extends Stage {

    private ContentLayout contentLayout;
    private final EditorMenuBar menuBar;
    private final EditorToolBar toolBar;

    private final ValueListenerManager<ContentLayout> contentLayoutValueListeners = new ValueListenerManager<>();
    private final ValueListenerManager<GameModel> gameModelValueListeners = new ValueListenerManager<>();

    private final GameBoardViewer boardViewer;
    private final EditorActionToolBar editorActionToolBar;
    private final GameBoardViewerComposite compositeViewer;
    private final GameTreeViewer treeViewer;
    private GameModel gameModel;

    public EditorFrame(GameModel gameModel) {
        this(gameModel, ContentLayout.REVIEW);
    }

    public EditorFrame(GameModel gameModel, ContentLayout layout) {
        var treeViewerSettings = new GameTreeViewerSettings();
        // TODO: Extract these out to settings.json
        treeViewerSettings.setBackgroundColor(GuiUtilities.getColor(43, 43, 43));
        treeViewerSettings.setNodeColor(GuiUtilities.getColor(90, 90, 90));
        treeViewerSettings.setNodeHoverColor(GuiUtilities.getColor(170, 170, 170));
        treeViewerSettings.setNodeInCurrentVariationColor(GuiUtilities.getColor(203, 203, 203));
        treeViewerSettings.setNodeWithCommentaryColor(GuiUtilities.getColor(87, 125, 186));
        treeViewerSettings.setCurrentNodeColor(GuiUtilities.getColor(255, 255, 255));

        var boardSettings = Settings.getCurrentGameBoardSettings();
        boardViewer = new GameBoardViewer(boardSettings);
        enableDragAndDropToOpenFile(boardViewer);

        treeViewer = new GameTreeViewer();
        treeViewer.setSettings(treeViewerSettings);

        menuBar = new EditorMenuBar(this);
        toolBar = new EditorToolBar();

        editorActionToolBar = new EditorActionToolBar();
        editorActionToolBar.addToolSelectionListener(this::setTool);

        compositeViewer = new GameBoardViewerComposite(boardViewer, editorActionToolBar);

        setLayout(layout);
        setGameModel(gameModel);
        setTitle(Yi.getProgramName());
    }

    private void enableDragAndDropToOpenFile(GameBoardViewer boardViewer) {
        boardViewer.setDragAndDropBehaviour(files -> {
            var success = false;
            if (files.size() == 1) {
                var file = files.get(0);
                try {
                    var importedGameModel = GameModelImporter.INSTANCE.fromFile(file.toPath());
                    setGameModel(importedGameModel);
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
        if (this.gameModel != null) {
            this.gameModel.dispose();
        }
        this.gameModel = newModel;
        this.gameModel.getInfo().addChangeListener(editorActionToolBar::onGameInfoUpdate);

        boardViewer.setGameModel(newModel);
        treeViewer.setGameModel(newModel);
        editorActionToolBar.onGameModelChange(newModel);

        gameModelValueListeners.fireValueChanged(newModel);
    }

    public @NotNull GameModel getGameModel() {
        Objects.requireNonNull(gameModel, "No game model is set. " +
                "setGameModel() must be called once before calling getGameModel().");

        return gameModel;
    }

    private void setTool(EditorTool tool) {
        tool.apply(boardViewer);
    }

    private boolean addedMenuBarOnce = false;
    public void setLayout(@NotNull ContentLayout newLayout) {
        if (this.contentLayout == newLayout) {
            return; // Avoid flickering when setting the same layout
        }
        editorActionToolBar.setContentForLayout(newLayout, gameModel);

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
            setHeight(currentHeight);
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

        this.contentLayout = newLayout;
        this.contentLayoutValueListeners.fireValueChanged(this.contentLayout);
    }

    /**
     * Wrapper for {@link #setScene(Scene)} but also runs some custom routines.
     * It is highly recommended to use this method rather than the base {@link #setScene(Scene)}
     * so that scenes support as many editor features as possible.
     *
     * @param newScene Scene to set.
     */
    private void setYiScene(@NotNull YiScene newScene) {
        installUndoRedoAccelerators(newScene);
        setScene(newScene);
    }

    private void installUndoRedoAccelerators(YiScene newScene) {
        AcceleratorManager.getAccelerator(AcceleratorId.UNDO).install(newScene, boardViewer::requestUndo);
        AcceleratorManager.getAccelerator(AcceleratorId.REDO).install(newScene, boardViewer::requestRedo);
    }

    public Parent getBoardComponent() {
        return compositeViewer;
    }

    public Parent getTreeComponent() {
        return treeViewer.getComponent();
    }

    public @Nullable ContentLayout getContentLayout() {
        return contentLayout;
    }

    public void addContentLayoutChangeListener(ValueListener<ContentLayout> listener) {
        contentLayoutValueListeners.addListener(listener);
    }

    public void addGameModelChangeListener(ValueListener<GameModel> listener) {
        gameModelValueListeners.addListener(listener);
    }
}
