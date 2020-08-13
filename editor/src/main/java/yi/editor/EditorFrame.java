package yi.editor;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import yi.component.YiScene;
import yi.component.board.GameBoardViewer;
import yi.component.gametree.GameTreeViewer;
import yi.component.gametree.GameTreeViewerSettings;
import yi.component.utilities.GuiUtilities;
import yi.core.go.GameModel;
import yi.editor.components.ContentLayout;
import yi.editor.components.ControlToolBar;
import yi.editor.components.EditorMenuBar;
import yi.editor.components.EditorToolBar;
import yi.editor.settings.Settings;

/**
 * The main frame for an editor session.
 */
public class EditorFrame extends Stage {

    private final GlobalAccelerators globalAccelerators;
    private final EditorMenuBar menuBar;
    private final EditorToolBar editorToolBar;
    private final ControlToolBar controlToolBar;

    private final GameBoardViewer boardViewer;
    private final GameTreeViewer treeViewer;
    private final GameTreeViewerSettings treeViewerSettings;
    private GameModel gameModel;

    public EditorFrame(GameModel gameModel) {
        this(gameModel, Settings.general.getCurrentLayout());
    }

    public EditorFrame(GameModel gameModel, ContentLayout layout) {
        this.gameModel = gameModel;
        this.globalAccelerators = new GlobalAccelerators();
        this.globalAccelerators.setUndoSystemHandler(new DefaultUndoSystemHandler());

        boardViewer = new GameBoardViewer(Settings.getCurrentGameBoardSettings());
        boardViewer.setGameModel(gameModel);

        treeViewerSettings = new GameTreeViewerSettings();
        treeViewerSettings.setBackgroundColor(GuiUtilities.getNormalized(43, 43, 43));
        treeViewerSettings.setNodeColor(GuiUtilities.getNormalized(90, 90, 90));
        treeViewerSettings.setNodeHoverColor(GuiUtilities.getNormalized(170, 170, 170));
        treeViewerSettings.setNodeInCurrentVariationColor(GuiUtilities.getNormalized(203, 203, 203));
        treeViewerSettings.setCurrentNodeColor(GuiUtilities.getNormalized(255, 255, 255));

        treeViewer = new GameTreeViewer(gameModel);
        treeViewer.setSettings(treeViewerSettings);

        menuBar = new EditorMenuBar();

        editorToolBar = new EditorToolBar();
        editorToolBar.addToolSelectionListener(this::setTool);

        controlToolBar = new ControlToolBar();
        controlToolBar.addLayoutOptionsValueListener(this::setLayout);

        setLayout(layout);
    }

    private void setTool(EditorTool tool) {
        tool.apply(boardViewer);
    }

    private void setLayout(ContentLayout newLayout) {
        editorToolBar.setButtonsForContentLayout(newLayout);

        var content = newLayout.getContent(this);

        var controlPane = new BorderPane();
        controlPane.setTop(menuBar);

        var toolBarPane = new BorderPane();
        {
            toolBarPane.setCenter(editorToolBar);
            toolBarPane.setLeft(controlToolBar);

            toolBarPane.heightProperty().addListener(newHeight -> {
                editorToolBar.setPrefHeight(toolBarPane.getHeight());
                controlToolBar.setMinHeight(toolBarPane.getHeight());
            });
        }
        controlPane.setCenter(toolBarPane);

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

    public GameBoardViewer getBoardViewer() {
        return boardViewer;
    }

    public GameTreeViewer getTreeViewer() {
        return treeViewer;
    }


    class DefaultUndoSystemHandler implements GlobalAccelerators.UndoSystemHandler {

        @Override
        public void requestUndo() {
            if (boardViewer != null) {
                boardViewer.requestUndo();
            }
        }

        @Override
        public void requestRedo() {
            if (boardViewer != null) {
                boardViewer.requestRedo();
            }
        }
    }
}
