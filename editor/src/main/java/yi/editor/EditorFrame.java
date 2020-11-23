package yi.editor;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import yi.component.YiScene;
import yi.component.board.GameBoardViewer;
import yi.component.gametree.GameTreeViewer;
import yi.component.gametree.GameTreeViewerSettings;
import yi.component.utilities.GuiUtilities;
import yi.core.go.GameModel;
import yi.core.go.GameModelImporter;
import yi.core.go.GameParseException;
import yi.editor.components.ContentLayout;
import yi.editor.components.EditorMenuBar;
import yi.editor.components.EditorToolBar;
import yi.editor.settings.Settings;

import java.io.IOException;

/**
 * The main frame for an editor session.
 */
public class EditorFrame extends Stage {

    private final GlobalAccelerators globalAccelerators;
    private final EditorMenuBar menuBar;
    private final EditorToolBar editorToolBar;

    private final GameBoardViewer boardViewer;
    private final GameTreeViewer treeViewer;
    private final GameTreeViewerSettings treeViewerSettings;
    private GameModel gameModel;

    public EditorFrame(GameModel gameModel) {
        this(gameModel, ContentLayout.REVIEW);
    }

    public EditorFrame(GameModel gameModel, ContentLayout layout) {
        this.gameModel = gameModel;
        this.globalAccelerators = new GlobalAccelerators();
        this.globalAccelerators.setUndoSystemHandler(new DefaultUndoSystemHandler());

        treeViewerSettings = new GameTreeViewerSettings();
        treeViewerSettings.setBackgroundColor(GuiUtilities.getColor(43, 43, 43));
        treeViewerSettings.setNodeColor(GuiUtilities.getColor(90, 90, 90));
        treeViewerSettings.setNodeHoverColor(GuiUtilities.getColor(170, 170, 170));
        treeViewerSettings.setNodeInCurrentVariationColor(GuiUtilities.getColor(203, 203, 203));
        treeViewerSettings.setCurrentNodeColor(GuiUtilities.getColor(255, 255, 255));

        var boardSettings = Settings.getCurrentGameBoardSettings();
        boardViewer = new GameBoardViewer(boardSettings);
        enableDragAndDropToOpenFile(boardViewer);

        treeViewer = new GameTreeViewer();
        treeViewer.setSettings(treeViewerSettings);

        menuBar = new EditorMenuBar(this);

        editorToolBar = new EditorToolBar();
        editorToolBar.addToolSelectionListener(this::setTool);

        setLayout(layout);
        setGameModel(gameModel);
//        initStyle(StageStyle.UNDECORATED);
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

    public void setGameModel(@NotNull GameModel gameModel) {
        boardViewer.setGameModel(gameModel);
        treeViewer.setGameModel(gameModel);
    }

    private void setTool(EditorTool tool) {
        tool.apply(boardViewer);
    }

    private void setLayout(ContentLayout newLayout) {
        editorToolBar.setButtonsForContentLayout(newLayout);

        var content = newLayout.getContent(this);

        var controlPane = new BorderPane();
        controlPane.setTop(menuBar);
        controlPane.setBottom(editorToolBar);

        var container = new BorderPane();
        container.setTop(controlPane);
        container.setCenter(content);

        var currentScene = getScene();

        if (currentScene != null) {
            double currentHeight = currentScene.getHeight();
            double newAspectRatio = newLayout.getPreferredAspectRatio();
            double newWidth = currentHeight * newAspectRatio;

            var newScene = new YiScene(container, newWidth, currentHeight);
            globalAccelerators.installFor(newScene);
            setScene(newScene);
            setWidth(newWidth);
            setHeight(currentHeight);
        } else {
            var startupSize = newLayout.getMinimumWindowSize();

            double startupWidth = startupSize.getWidth();
            double startupHeight = startupSize.getHeight();

            var newScene = new YiScene(container, startupWidth, startupHeight);
            globalAccelerators.installFor(newScene);
            setScene(newScene);
        }

        var minSize = newLayout.getMinimumWindowSize();
        setMinWidth(minSize.getWidth());
        setMinHeight(minSize.getHeight());
    }

    public Parent getBoardComponent() {
        return boardViewer.getComponent();
    }

    public Parent getTreeComponent() {
        return treeViewer.getComponent();
    }

    class DefaultUndoSystemHandler implements GlobalAccelerators.UndoSystemHandler {

        @Override
        public void requestUndo() {
            boardViewer.requestUndo();
        }

        @Override
        public void requestRedo() {
            boardViewer.requestRedo();
        }
    }
}
